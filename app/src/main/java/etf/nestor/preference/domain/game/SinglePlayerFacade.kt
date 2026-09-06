package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.bidding.BiddingManager
import etf.nestor.preference.domain.bot.BotBidStatus
import etf.nestor.preference.domain.bot.BotPlayer
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.model.Trick
import etf.nestor.preference.domain.rules.MoveValidator
import etf.nestor.preference.domain.scoring.GameType
import etf.nestor.preference.domain.scoring.Multiplier
import etf.nestor.preference.domain.scoring.PlayerRoundResult
import etf.nestor.preference.domain.scoring.RoundOutcome
import etf.nestor.preference.domain.scoring.ScoreCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class SinglePlayerFacade(
    playerName: String = "Igrač",
    startScore: Int = 120,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    savedState: SavedGameState? = null
) {

    companion object {
        const val HUMAN = 0
        const val BOT_1 = 1
        const val BOT_2 = 2

        var BOT_MIN_THINK_MS = 1500L
        var BOT_MAX_THINK_MS = 2500L

        var BOT_SAY_READ_DELAY_MS = 1200L

        const val TRICK_SHOW_DELAY_MS = 2000L
        const val TALON_SHOW_DELAY_MS = 1500L

        const val HAND_TRANSITION_DELAY_MS = 2000L
    }

    private val players = listOf(
        Player(savedState?.playerNames?.getOrNull(HUMAN) ?: playerName, HUMAN),
        Player(savedState?.playerNames?.getOrNull(BOT_1) ?: "Bot 1", BOT_1),
        Player(savedState?.playerNames?.getOrNull(BOT_2) ?: "Bot 2", BOT_2)
    )

    private val gameState = GameState(players, startScore)
    private val scoreCalc = ScoreCalculator()
    private val deck = Deck()

    private var biddingManager: BiddingManager? = null
    private var followingManager: FollowingManager? = null
    private var gameEngine: GameEngine? = null
    private var talon: Talon? = null
    private var minimumBidValue: Int = 2
    private var currentGameType: BidType? = null

    private var lastCompletedTrick: List<PlayedCard> = emptyList()
    private var showingCompletedTrick: Boolean = false

    private val wonTricksByPlayer: MutableMap<Int, MutableList<List<PlayingCard>>> = mutableMapOf()

    private val botBidStatus: MutableMap<Int, BotBidStatus> = mutableMapOf()

    private var botJob: Job? = null

    private var gameOver: Boolean = false

    private val _state = MutableStateFlow(buildSnapshot(GameMessage.Loading))
    val state: StateFlow<GameSnapshot> = _state.asStateFlow()

    init {
        if (savedState != null) restoreFrom(savedState) else startNewHand(isFirstHand = true)
    }

    fun playerBid(action: BidAction) {
        val hand = currentHand()
        check(hand.phase == HandPhase.BIDDING) { "Nije faza licitiranja" }
        check(biddingManager!!.currentPlayer == HUMAN) { "Nije red coveku" }

        biddingManager!!.bid(HUMAN, action)

        if (biddingManager!!.isFinished()) handleBiddingFinished()
        else launchBotBidding()
    }

    fun playerPickupTalon() {
        val hand = currentHand()
        check(hand.phase == HandPhase.TALON_PICKUP)
        check(hand.declarer == HUMAN)

        players[HUMAN].addCards(talon!!.cards)
        hand.moveToPhase(HandPhase.DISCARDING)
        emit(GameMessage.TookTalon)
    }

    fun playerDiscard(cards: List<PlayingCard>) {
        val hand = currentHand()
        check(hand.phase == HandPhase.DISCARDING)
        check(hand.declarer == HUMAN)
        check(cards.size == 2) { "Mora da se bace tacno 2 karte" }
        check(players[HUMAN].hand.containsAll(cards)) { "Igrač nema te karte" }

        cards.forEach { players[HUMAN].playCard(it) }
        hand.moveToPhase(HandPhase.TRUMP_DECLARATION)
        emit(GameMessage.DeclareGamePrompt)
    }

    fun playerDeclareGame(gameType: BidType) {
        val hand = currentHand()
        check(hand.phase == HandPhase.TRUMP_DECLARATION)
        check(hand.declarer == HUMAN)
        check(gameType.strength >= minimumBidValue) {
            "Mora se igrati najmanje ${minimumBidValue}, najavljeno ${gameType.strength}"
        }

        currentGameType = gameType
        proceedToFollowing(hand, gameType)
    }

    fun playerFollow(follows: Boolean) {
        val hand = currentHand()
        check(hand.phase == HandPhase.FOLLOW)
        check(followingManager!!.currentPlayer == HUMAN) { "Nije red coveku za pracenje" }

        followingManager!!.decide(HUMAN, follows)

        if (followingManager!!.isFinished) handleFollowingFinished()
        else launchBotFollowing()
    }

    fun playerPlayCard(card: PlayingCard) {
        val hand = currentHand()
        check(hand.phase == HandPhase.PLAYING)
        check(hand.currentPlayer == HUMAN) { "Nije red coveku" }

        val humanHand = players[HUMAN].hand
        val trick = hand.currentTrick!!
        if (!MoveValidator.isValidMove(humanHand, card, trick)) {
            val legalCount = humanHand.count { MoveValidator.isValidMove(humanHand, it, trick) }
            emit(GameMessage.IllegalMove(legalCount))
            return
        }

        val isLastCardOfTrick = trick.playedCards.size == 2
        val trickBefore = trick.playedCards.toList()
        val tricksWonBefore = players.map { it.tricksWon }

        gameEngine!!.playTurn(PlayedCard(HUMAN, card))

        if (isLastCardOfTrick) {
            val completedTrick = trickBefore + PlayedCard(HUMAN, card)
            recordCompletedTrick(completedTrick, tricksWonBefore)

            lastCompletedTrick = completedTrick
            showingCompletedTrick = true
            emit(GameMessage.TrickFinished)
            botJob?.cancel()
            botJob = scope.launch {
                delay(TRICK_SHOW_DELAY_MS.milliseconds)
                showingCompletedTrick = false
                if (hand.isFinished()) {
                    finishHand()
                } else {
                    emit(GameMessage.Continuing)
                    playBotsSequentially()
                }
            }
        } else {
            emit(GameMessage.YouPlayedCard)
            if (hand.isFinished()) finishHand()
            else launchBotPlay()
        }
    }

    fun captureSaveState(): SavedGameState {
        val hand = currentHand()
        val fm = followingManager

        return SavedGameState(
            playerNames = players.map { it.name },
            dealerPosition = gameState.dealerPosition,
            scoreSheets = gameState.scoreSheets.toMap(),
            playerHands = players.map { it.hand.toList() },
            tricksWon = players.map { it.tricksWon },
            talonCards = talon?.cards?.toList() ?: emptyList(),
            minimumBidValue = minimumBidValue,
            currentGameType = currentGameType,
            wonTricksByPlayer = wonTricksByPlayer.mapValues { it.value.toList() },
            phase = hand.phase,
            declarer = hand.declarer,
            trumpSuit = hand.trumpSuit,
            handCurrentPlayer = hand.currentPlayer,
            bids = biddingManager?.getAllBids() ?: emptyList(),
            finishedTricks = hand.finishedTricks.map { it.playedCards.toList() },
            currentTrickCards = hand.currentTrick?.playedCards?.toList() ?: emptyList(),
            followRightDecision = fm?.let { it.getDecision(it.rightPlayer) },
            followLeftDecision = fm?.let { it.getDecision(it.leftPlayer) }
        )
    }

    private fun restoreFrom(save: SavedGameState) {
        gameState.setDealerPosition(save.dealerPosition)
        save.scoreSheets.forEach { (pos, sheet) -> gameState.scoreSheets[pos] = sheet }

        players.forEachIndexed { i, p ->
            p.clearHand()
            p.resetTricks()
            p.addCards(save.playerHands.getOrElse(i) { emptyList() })
            repeat(save.tricksWon.getOrElse(i) { 0 }) { p.wonTrick() }
        }

        talon = Talon(save.talonCards.toMutableList())
        minimumBidValue = save.minimumBidValue
        currentGameType = save.currentGameType
        wonTricksByPlayer.clear()
        save.wonTricksByPlayer.forEach { (pos, tricks) ->
            wonTricksByPlayer[pos] = tricks.toMutableList()
        }
        botBidStatus.clear()

        val firstPlayer = (save.dealerPosition + 1) % 3

        val finishedTricks = save.finishedTricks.map { cards ->
            Trick(save.trumpSuit).apply { cards.forEach { playCard(it.playerPosition, it.card) } }
        }
        val currentTrick = if (save.phase == HandPhase.PLAYING) {
            Trick(save.trumpSuit).apply {
                save.currentTrickCards.forEach {
                    playCard(
                        it.playerPosition,
                        it.card
                    )
                }
            }
        } else null

        val hand = HandState(players, firstPlayer)
        hand.restoreState(
            phase = save.phase,
            declarer = save.declarer,
            trumpSuit = save.trumpSuit,
            currentPlayer = save.handCurrentPlayer,
            currentTrick = currentTrick,
            finishedTricks = finishedTricks
        )
        gameState.startHand(hand)

        biddingManager = BiddingManager(firstPlayer).apply {
            save.bids.forEach { bid(it.playerPosition, it.action) }
        }

        followingManager = save.declarer
            ?.takeIf { save.phase == HandPhase.FOLLOW || save.phase == HandPhase.PLAYING }
            ?.let { declarer ->
                FollowingManager(declarer).apply {
                    save.followRightDecision?.let { decide(rightPlayer, it) }
                    save.followLeftDecision?.let { decide(leftPlayer, it) }
                }
            }

        gameEngine = if (save.phase == HandPhase.PLAYING) GameEngine(hand) else null

        emit(GameMessage.Continuing)
        resumeAfterRestore(hand)
    }

    private fun resumeAfterRestore(hand: HandState) {
        when (hand.phase) {
            HandPhase.BIDDING ->
                if (biddingManager?.currentPlayer != HUMAN) launchBotBidding()

            HandPhase.TALON_PICKUP, HandPhase.DISCARDING, HandPhase.TRUMP_DECLARATION -> {
                val declarer = hand.declarer
                if (declarer != null && declarer != HUMAN) {
                    botJob?.cancel()
                    botJob = scope.launch { botHandleTalonAndDeclare(declarer) }
                }
            }

            HandPhase.FOLLOW ->
                if (followingManager?.currentPlayer != HUMAN) launchBotFollowing()

            HandPhase.PLAYING ->
                if (!hand.isFinished() && hand.currentPlayer != HUMAN) launchBotPlay()

            HandPhase.FINISHED -> Unit
        }
    }

    private fun startNewHand(isFirstHand: Boolean = false) {
        botJob?.cancel()
        showingCompletedTrick = false
        lastCompletedTrick = emptyList()
        wonTricksByPlayer.clear()
        botBidStatus.clear()

        players.forEach { it.clearHand(); it.resetTricks() }
        deck.reset()

        val dealResult = Dealer.deal(deck)
        players.forEachIndexed { i, p -> p.addCards(dealResult.playersCards[i]) }
        talon = dealResult.talon

        val firstPlayer = (gameState.dealerPosition + 1) % 3
        biddingManager = BiddingManager(firstPlayer)
        followingManager = null
        gameEngine = null
        currentGameType = null
        minimumBidValue = 2

        val handState = HandState(players, firstPlayer)
        gameState.startHand(handState)

        val dealerName = players[gameState.dealerPosition].name
        val firstAskedName = players[firstPlayer].name
        if (firstPlayer != HUMAN) botBidStatus[firstPlayer] = BotBidStatus.Thinking

        if (isFirstHand) {
            emit(GameMessage.NewRound(dealerName, firstAskedName))
            launchBotBidding()
        } else {

            botJob = scope.launch {
                delay(HAND_TRANSITION_DELAY_MS.milliseconds)
                emit(GameMessage.NewRound(dealerName, firstAskedName))
                launchBotBidding()
            }
        }
    }

    private fun launchBotBidding() {
        botJob?.cancel()
        botJob = scope.launch { processBotBidding() }
    }

    private suspend fun processBotBidding() {
        val bm = biddingManager!!
        while (!bm.isFinished() && bm.currentPlayer != HUMAN) {
            val botPos = bm.currentPlayer
            botBidStatus[botPos] = BotBidStatus.Thinking
            emit(GameMessage.BotAskedToBid(players[botPos].name))
            delay(botThinkDelay().milliseconds)

            val saysGood = bm.currentWinner == HUMAN && bm.currentHighestBid == 2
            bm.bid(botPos, BidAction.PASS)
            botBidStatus[botPos] = if (saysGood) BotBidStatus.Good else BotBidStatus.Pass
            emit(
                if (saysGood) GameMessage.BotSaysGood(players[botPos].name)
                else GameMessage.BotPasses(players[botPos].name)
            )
            delay(BOT_SAY_READ_DELAY_MS.milliseconds)
        }
        if (bm.isFinished()) handleBiddingFinished()
        else emit(GameMessage.YourTurnBid)
    }

    private fun handleBiddingFinished() {
        val result = biddingManager!!.getResult()
        val hand = currentHand()

        if (result.everyonePassed) {
            emit(GameMessage.AllPassed)
            gameState.finishHand()
            startNewHand()
            return
        }

        val winner = result.winner!!
        minimumBidValue = result.winningValue!!
        hand.setDeclarer(winner)

        if (winner == HUMAN) {
            hand.moveToPhase(HandPhase.TALON_PICKUP)
            emit(GameMessage.YouWonBid(minimumBidValue))
        } else {
            emit(GameMessage.BotWonBid(winner))
            botJob?.cancel()
            botJob = scope.launch { botHandleTalonAndDeclare(winner) }
        }
    }

    private suspend fun botHandleTalonAndDeclare(botPos: Int) {
        val hand = currentHand()
        val bot = players[botPos]

        if (hand.phase != HandPhase.DISCARDING && hand.phase != HandPhase.TRUMP_DECLARATION) {
            hand.moveToPhase(HandPhase.TALON_PICKUP)
            emit(GameMessage.BotTakesTalon(bot.name))
            delay(TALON_SHOW_DELAY_MS.milliseconds)

            bot.addCards(talon!!.cards)
            hand.moveToPhase(HandPhase.DISCARDING)
        }

        if (hand.phase == HandPhase.DISCARDING) {
            delay(botThinkDelay().milliseconds)
            val toDiscard = bot.hand.shuffled().take(2)
            toDiscard.forEach { bot.playCard(it) }
            hand.moveToPhase(HandPhase.TRUMP_DECLARATION)
        }

        delay(botThinkDelay().milliseconds)
        val gameType = currentGameType ?: BidType.fromValue(minimumBidValue) ?: BidType.SPADES
        currentGameType = gameType

        proceedToFollowing(hand, gameType)
    }

    private fun proceedToFollowing(hand: HandState, gameType: BidType) {
        val trumpSuit = gameTypeToSuit(gameType)
        hand.setTrumpSuit(trumpSuit)
        hand.moveToPhase(HandPhase.FOLLOW)

        followingManager = FollowingManager(hand.declarer!!)

        if (gameType == BidType.MISERE) {
            val fm = followingManager!!
            fm.decide(fm.rightPlayer, true)
            fm.decide(fm.leftPlayer, true)
            emit(GameMessage.BotPlaysMisere(players[hand.declarer!!].name))
            startPlayingPhase()
            return
        }

        emit(GameMessage.BotPlaysGame(players[hand.declarer!!].name, gameType))
        launchBotFollowing()
    }

    private fun launchBotFollowing() {
        botJob?.cancel()
        botJob = scope.launch { processFollowingBots() }
    }

    private suspend fun processFollowingBots() {
        val fm = followingManager!!
        while (!fm.isFinished && fm.currentPlayer != HUMAN) {
            val botPos = fm.currentPlayer
            emit(GameMessage.BotAskedToFollow(players[botPos].name))
            delay(botThinkDelay().milliseconds)
            fm.decide(botPos, true)
            emit(GameMessage.BotFollows(players[botPos].name))
            delay(BOT_SAY_READ_DELAY_MS.milliseconds)
        }
        if (fm.isFinished) handleFollowingFinished()
        else emit(GameMessage.AskFollow)
    }

    private fun handleFollowingFinished() {
        val fm = followingManager!!
        if (fm.nobodyFollows()) {
            emit(GameMessage.NobodyFollows)
            finishHandNobodyFollows()
        } else {
            val followers = fm.getFollowers()
            emit(GameMessage.FollowersJoin(followers.joinToString { players[it].name }))
            startPlayingPhase()
        }
    }

    private fun startPlayingPhase() {
        val hand = currentHand()
        hand.startPlayingPhase()
        gameEngine = GameEngine(hand)
        hand.setCurrentPlayer((gameState.dealerPosition + 1) % 3)
        emit(GameMessage.GameStarted)
        launchBotPlay()
    }

    private fun launchBotPlay() {
        botJob?.cancel()
        botJob = scope.launch { playBotsSequentially() }
    }

    private suspend fun playBotsSequentially() {
        val hand = currentHand()

        while (hand.phase == HandPhase.PLAYING && !hand.isFinished() && hand.currentPlayer != HUMAN) {
            delay(botThinkDelay().milliseconds)

            val botPos = hand.currentPlayer
            val bot = players[botPos]
            val trick = hand.currentTrick ?: break
            val isLastCardOfTrick = trick.playedCards.size == 2
            val trickBefore = trick.playedCards.toList()
            val tricksWonBefore = players.map { it.tricksWon }

            val card = BotPlayer.decideCard(bot.hand, trick)
            gameEngine!!.playTurn(PlayedCard(botPos, card))

            if (isLastCardOfTrick) {
                val completedTrick = trickBefore + PlayedCard(botPos, card)
                recordCompletedTrick(completedTrick, tricksWonBefore)

                lastCompletedTrick = completedTrick
                showingCompletedTrick = true
                emit(GameMessage.TrickFinished)
                delay(TRICK_SHOW_DELAY_MS.milliseconds)
                showingCompletedTrick = false
            } else {
                emit(GameMessage.BotPlayedCard(players[botPos].name))
            }

            if (hand.isFinished()) {
                finishHand()
                return
            }
        }

        if (!hand.isFinished()) {
            emit(GameMessage.YourTurn)
        }
    }

    private fun finishHand() {
        val hand = currentHand()
        val gameType = currentGameType ?: return
        val declarerPos = hand.declarer ?: return

        val gt = bidTypeToGameType(gameType)
        val results = players.map { PlayerRoundResult(it.position, it.tricksWon) }
        val outcome = RoundOutcome(
            gamePlayer = declarerPos,
            gameType = gt,
            multiplier = Multiplier.NONE,
            playerResults = results
        )
        scoreCalc.applyRound(outcome, gameState.scoreSheets)

        val declarerTricks = players[declarerPos].tricksWon
        val won = if (gameType == BidType.MISERE) declarerTricks == 0 else declarerTricks >= 6
        emit(GameMessage.RoundFinished(players[declarerPos].name, won))

        if (endGameIfOver()) return

        gameState.finishHand()
        startNewHand()
    }

    private fun finishHandNobodyFollows() {
        val hand = currentHand()
        val gameType = currentGameType ?: return
        val declarerPos = hand.declarer ?: return

        val gt = bidTypeToGameType(gameType)
        val results = players.map { PlayerRoundResult(it.position, 0) }
        val outcome = RoundOutcome(
            gamePlayer = declarerPos,
            gameType = gt,
            multiplier = Multiplier.NONE,
            playerResults = results
        )
        scoreCalc.applyRound(outcome, gameState.scoreSheets)

        if (endGameIfOver()) return

        gameState.finishHand()
        startNewHand()
    }

    private fun endGameIfOver(): Boolean {
        val sum = gameState.scoreSheets.values.sumOf { it.center }
        if (sum > 0) return false

        gameOver = true
        botJob?.cancel()
        botJob = scope.launch {
            delay(HAND_TRANSITION_DELAY_MS.milliseconds)
            emit(GameMessage.GameOver)
        }
        return true
    }

    private fun recordCompletedTrick(
        completedCards: List<PlayedCard>,
        tricksWonBefore: List<Int>
    ) {
        val winner = players.indexOfFirst { it.tricksWon > tricksWonBefore[it.position] }
        if (winner >= 0) {
            wonTricksByPlayer.getOrPut(winner) { mutableListOf() }
                .add(completedCards.map { it.card })
        }
    }

    private fun botThinkDelay(): Long {
        val min = BOT_MIN_THINK_MS
        val max = BOT_MAX_THINK_MS
        return if (min >= max) min else Random.nextLong(min, max + 1)
    }

    private fun gameTypeToSuit(gameType: BidType): Suit = when (gameType) {
        BidType.SPADES -> Suit.SPADES
        BidType.DIAMONDS -> Suit.DIAMONDS
        BidType.HEARTS -> Suit.HEARTS
        BidType.CLUBS -> Suit.CLUBS
        BidType.MISERE, BidType.SANS -> Suit.NO_SUIT
    }

    private fun bidTypeToGameType(gameType: BidType): GameType = when (gameType) {
        BidType.SPADES -> GameType.SPADES
        BidType.DIAMONDS -> GameType.DIAMONDS
        BidType.HEARTS -> GameType.HEARTS
        BidType.CLUBS -> GameType.CLUBS
        BidType.MISERE -> GameType.MISERE
        BidType.SANS -> GameType.SANS
    }

    private fun currentHand(): HandState = gameState.currentHand!!

    private fun emit(message: GameMessage) {
        _state.value = buildSnapshot(message)
    }

    private fun buildSnapshot(message: GameMessage): GameSnapshot {
        val hand = gameState.currentHand
        val fm = followingManager
        val bm = biddingManager

        val followStatus: Map<Int, Boolean?> = if (fm != null) {
            mapOf(
                fm.rightPlayer to fm.getDecision(fm.rightPlayer),
                fm.leftPlayer to fm.getDecision(fm.leftPlayer)
            )
        } else emptyMap()

        val talonVisible = hand?.phase == HandPhase.TALON_PICKUP

        val isHumanTurn = if (showingCompletedTrick) false else when (hand?.phase) {
            HandPhase.BIDDING -> bm?.currentPlayer == HUMAN
            HandPhase.TALON_PICKUP -> hand.declarer == HUMAN
            HandPhase.DISCARDING -> hand.declarer == HUMAN
            HandPhase.TRUMP_DECLARATION -> hand.declarer == HUMAN
            HandPhase.FOLLOW -> fm?.currentPlayer == HUMAN

            HandPhase.PLAYING -> !hand.isFinished() && hand.currentPlayer == HUMAN
            else -> false
        }

        val legalCards =
            if (!showingCompletedTrick && hand?.phase == HandPhase.PLAYING && hand.currentPlayer == HUMAN) {
                val humanHand = players[HUMAN].hand
                val trick = hand.currentTrick
                if (trick != null) humanHand.filter {
                    MoveValidator.isValidMove(
                        humanHand,
                        it,
                        trick
                    )
                }
                else humanHand.toList()
            } else emptyList()

        val sortedHand = players[HUMAN].hand
            .sortedWith(compareBy({ it.suit.ordinal }, { it.rank.power }))

        val trickToShow = if (showingCompletedTrick) lastCompletedTrick
        else hand?.currentTrick?.playedCards?.toList() ?: emptyList()

        return GameSnapshot(
            phase = hand?.phase ?: HandPhase.BIDDING,
            dealerPosition = gameState.dealerPosition,
            playerNames = players.map { it.name },
            playerHand = sortedHand,
            talon = if (talonVisible) talon?.cards?.toList() ?: emptyList() else emptyList(),
            currentTrick = trickToShow,
            tricksWon = players.map { it.tricksWon },
            handSizes = players.map { it.handSize() },
            biddingHighestBid = bm?.currentHighestBid ?: 0,
            biddingCurrentWinner = bm?.currentWinner,
            biddingCurrentPlayer = bm?.currentPlayer,
            declarer = hand?.declarer,
            trumpSuit = hand?.trumpSuit,
            currentGameType = currentGameType,
            minimumBidValue = minimumBidValue,
            followStatus = followStatus,
            scoreSheets = gameState.scoreSheets.toMap(),
            message = message,
            isHumanTurn = isHumanTurn,
            legalCardsForHuman = legalCards,
            wonTricksByPlayer = wonTricksByPlayer.mapValues { it.value.toList() },
            biddingBotStatus = botBidStatus.toMap(),
            gameOver = gameOver
        )
    }
}

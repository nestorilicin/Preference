package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BiddingManager
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.bot.BotPlayer
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.Suit
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

/**
 * Centralna fasada za singleplayer partiju preferansa.
 *
 * Poziva se iz ViewModel-a. Interno drzi state machinu:
 *   BIDDING -> TALON_PICKUP -> DISCARDING -> TRUMP_DECLARATION -> FOLLOW -> PLAYING -> (nova ruka)
 *
 * Botovi uvek pasuju u licitiranju, uvek prate i igraju nasumicnu legalnu kartu.
 * Bot potezi imaju delay od 1s, a prikaz kompletnog stiha traje 1.5s pre resolvovanja.
 */
class SingleplayerFacade(
    playerName: String = "Igrač",
    startScore: Int = 120,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    companion object {
        const val HUMAN = 0
        const val BOT_1 = 1
        const val BOT_2 = 2
        const val BOT_DELAY_MS = 1000L       // delay između bot poteza
        const val TRICK_SHOW_DELAY_MS = 1500L // koliko se vidi kompletan stih
    }

    private val players = listOf(
        Player(playerName, HUMAN),
        Player("Bot 1", BOT_1),
        Player("Bot 2", BOT_2)
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

    // Čuvamo poslednji kompletni stih da ga prikažemo pre clearanja
    private var lastCompletedTrick: List<PlayedCard> = emptyList()
    private var showingCompletedTrick: Boolean = false

    // Uzeti stihovi po igraču: playerPos -> lista stihova, svaki stih = lista 3 karte
    private val wonTricksByPlayer: MutableMap<Int, MutableList<List<PlayingCard>>> = mutableMapOf()

    // Job za bot igranje — cancelujemo stari pre nego što startujemo novi
    private var botJob: Job? = null

    private val _state = MutableStateFlow(buildSnapshot(GameMessage.Loading))
    val state: StateFlow<GameSnapshot> = _state.asStateFlow()

    init {
        startNewHand()
    }

    // ─── Javne metode — poziva ViewModel ─────────────────────────────────

    fun playerBid(action: BidAction) {
        val hand = currentHand()
        check(hand.phase == HandPhase.BIDDING) { "Nije faza licitiranja" }
        check(biddingManager!!.currentPlayer == HUMAN) { "Nije red coveku" }

        biddingManager!!.bid(HUMAN, action)

        if (biddingManager!!.isFinished()) handleBiddingFinished()
        else processBotBidding()
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
        else processFollowingBots()
    }

    fun playerPlayCard(card: PlayingCard) {
        val hand = currentHand()
        check(hand.phase == HandPhase.PLAYING)
        check(hand.currentPlayer == HUMAN) { "Nije red coveku" }

        // Validacija poteza
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
            // Sačuvaj kompletan stih i prikaži ga 1.5s pre nego što nastavimo
            lastCompletedTrick = completedTrick
            showingCompletedTrick = true
            emit(GameMessage.TrickFinished)
            botJob?.cancel()
            botJob = scope.launch {
                delay(TRICK_SHOW_DELAY_MS)
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

    // ─── Nova ruka ────────────────────────────────────────────────────────

    private fun startNewHand() {
        botJob?.cancel()
        showingCompletedTrick = false
        lastCompletedTrick = emptyList()
        wonTricksByPlayer.clear()

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

        emit(GameMessage.NewRound)
        processBotBidding()
    }

    // ─── Licitiranje ──────────────────────────────────────────────────────

    private fun processBotBidding() {
        val bm = biddingManager!!
        while (!bm.isFinished() && bm.currentPlayer != HUMAN) {
            bm.bid(bm.currentPlayer, BidAction.PASS)
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
            botHandleTalonAndDeclare(winner)
        }
    }

    // ─── Bot uzima kup i najavaljuje ──────────────────────────────────────

    private fun botHandleTalonAndDeclare(botPos: Int) {
        val hand = currentHand()
        val bot = players[botPos]

        hand.moveToPhase(HandPhase.TALON_PICKUP)
        bot.addCards(talon!!.cards)

        hand.moveToPhase(HandPhase.DISCARDING)
        val toDiscard = bot.hand.shuffled().take(2)
        toDiscard.forEach { bot.playCard(it) }

        hand.moveToPhase(HandPhase.TRUMP_DECLARATION)
        val gameType = BidType.fromValue(minimumBidValue) ?: BidType.SPADES
        currentGameType = gameType

        proceedToFollowing(hand, gameType)
    }

    // ─── Pracenje ─────────────────────────────────────────────────────────

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
        processFollowingBots()
    }

    private fun processFollowingBots() {
        val fm = followingManager!!
        while (!fm.isFinished && fm.currentPlayer != HUMAN) {
            fm.decide(fm.currentPlayer, true)
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

    // ─── Igranje ─────────────────────────────────────────────────────────

    private fun startPlayingPhase() {
        val hand = currentHand()
        hand.startPlayingPhase()
        gameEngine = GameEngine(hand)
        hand.setCurrentPlayer((gameState.dealerPosition + 1) % 3)
        emit(GameMessage.GameStarted)
        launchBotPlay()
    }

    /** Startuje bot igranje u coroutini (cancels prethodni job ako postoji) */
    private fun launchBotPlay() {
        botJob?.cancel()
        botJob = scope.launch { playBotsSequentially() }
    }

    /** Botovi igraju jedan po jedan sa delayom, uz pauzu za prikaz kompletnog stiha */
    private suspend fun playBotsSequentially() {
        val hand = currentHand()

        while (hand.phase == HandPhase.PLAYING && !hand.isFinished() && hand.currentPlayer != HUMAN) {
            delay(BOT_DELAY_MS) // Bot "razmislja"

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
                // Sačuvaj i prikaži kompletan stih
                lastCompletedTrick = completedTrick
                showingCompletedTrick = true
                emit(GameMessage.TrickFinished)
                delay(TRICK_SHOW_DELAY_MS)
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

    // ─── Kraj ruke i bodovanje ────────────────────────────────────────────

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

        gameState.finishHand()
        startNewHand()
    }

    // ─── Pracenje uzetih stihova ──────────────────────────────────────────

    /**
     * Poziva se nakon gameEngine.playTurn() kada je stih upravo završen.
     * Poredi tricksWon pre i posle da odredi pobednika i upisuje karte u wonTricksByPlayer.
     */
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

    // ─── Pomocne metode ───────────────────────────────────────────────────

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

        val talonVisible = hand?.phase == HandPhase.TALON_PICKUP && hand.declarer == HUMAN
        val isHumanTurn = when (hand?.phase) {
            HandPhase.BIDDING -> bm?.currentPlayer == HUMAN
            HandPhase.TALON_PICKUP -> hand.declarer == HUMAN
            HandPhase.DISCARDING -> hand.declarer == HUMAN
            HandPhase.TRUMP_DECLARATION -> hand.declarer == HUMAN
            HandPhase.FOLLOW -> fm?.currentPlayer == HUMAN
            HandPhase.PLAYING -> hand.currentPlayer == HUMAN
            else -> false
        }

        // Legalne karte tokom PLAYING faze — za highlight u UI-u
        val legalCards = if (hand?.phase == HandPhase.PLAYING && hand.currentPlayer == HUMAN) {
            val humanHand = players[HUMAN].hand
            val trick = hand.currentTrick
            if (trick != null) humanHand.filter { MoveValidator.isValidMove(humanHand, it, trick) }
            else humanHand.toList()
        } else emptyList()

        // Sortirana ruka igrača: po boji (SPADES, DIAMONDS, HEARTS, CLUBS), pa po jačini rastuće
        val sortedHand = players[HUMAN].hand
            .sortedWith(compareBy({ it.suit.ordinal }, { it.rank.power }))

        // Tekući stih: ako prikazujemo kompletan stih pre resolve-a, koristimo sačuvani
        val trickToShow = if (showingCompletedTrick) lastCompletedTrick
                          else hand?.currentTrick?.playedCards?.toList() ?: emptyList()

        return GameSnapshot(
            phase = hand?.phase ?: HandPhase.BIDDING,
            dealerPosition = gameState.dealerPosition,
            playerHand = sortedHand,
            talon = if (talonVisible) talon?.cards?.toList() ?: emptyList() else emptyList(),
            currentTrick = trickToShow,
            tricksWon = players.map { it.tricksWon },
            biddingHighestBid = bm?.currentHighestBid ?: 0,
            biddingCurrentWinner = bm?.currentWinner,
            biddingCurrentPlayer = bm?.currentPlayer,
            declarer = hand?.declarer,
            trumpSuit = hand?.trumpSuit,
            minimumBidValue = minimumBidValue,
            followStatus = followStatus,
            scoreSheets = gameState.scoreSheets.toMap(),
            message = message,
            isHumanTurn = isHumanTurn,
            legalCardsForHuman = legalCards,
            wonTricksByPlayer = wonTricksByPlayer.mapValues { it.value.toList() }
        )
    }
}

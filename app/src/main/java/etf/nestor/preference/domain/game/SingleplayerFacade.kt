package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BiddingManager
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.bidding.BotBidStatus
import etf.nestor.preference.domain.bot.BotPlayer
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Player
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

/**
 * Centralna fasada za singleplayer partiju preferansa.
 *
 * Poziva se iz ViewModel-a. Interno drzi state machinu:
 *   BIDDING -> TALON_PICKUP -> DISCARDING -> TRUMP_DECLARATION -> FOLLOW -> PLAYING -> (nova ruka)
 *
 * Botovi uvek pasuju u licitiranju, uvek prate i igraju nasumicnu legalnu kartu.
 * Pre svake bot odluke (licitacija, praćenje, bacanje/najava, igranje karte) botovi "razmišljaju"
 * nasumičan broj milisekundi između [BOT_MIN_THINK_MS] i [BOT_MAX_THINK_MS]. Prikaz kompletnog
 * stiha traje [TRICK_SHOW_DELAY_MS], a otkriveni kup pre nego sto ga bot uzme [TALON_SHOW_DELAY_MS].
 *
 * Ako se prosledi [savedState], partija se ne pokreće iznova nego se rekonstruiše iz njega
 * (Continue) — vidi [restoreFrom].
 */
class SingleplayerFacade(
    playerName: String = "Igrač",
    startScore: Int = 120,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    savedState: SavedGameState? = null
) {

    companion object {
        const val HUMAN = 0
        const val BOT_1 = 1
        const val BOT_2 = 2

        // Opseg "razmišljanja" bota pre svake odluke — var polja da bi po potrebi mogla
        // da se promene (npr. iz testova ili kasnijih podešavanja)
        var BOT_MIN_THINK_MS = 1500L
        var BOT_MAX_THINK_MS = 2500L

        // Koliko dugo ostaje vidljivo šta je bot rekao tokom licitacije (Dalje/Dobar), nakon
        // "razmišljanja" — da igrač stigne da pročita pre nego što se pređe na sledećeg igrača
        var BOT_SAY_READ_DELAY_MS = 1200L

        const val TRICK_SHOW_DELAY_MS = 2000L // koliko se vidi kompletan stih pre nego sto se sklone karte
        const val TALON_SHOW_DELAY_MS = 1500L // koliko se vidi otkriven kup pre nego sto ga bot uzme

        // Koliko ostaje vidljiva poruka o kraju prethodne ruke (Runda gotova / Refa) pre
        // nego što se najavi nova runda — vidi startNewHand()
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

    // Čuvamo poslednji kompletni stih da ga prikažemo pre clearanja
    private var lastCompletedTrick: List<PlayedCard> = emptyList()
    private var showingCompletedTrick: Boolean = false

    // Uzeti stihovi po igraču: playerPos -> lista stihova, svaki stih = lista 3 karte
    private val wonTricksByPlayer: MutableMap<Int, MutableList<List<PlayingCard>>> = mutableMapOf()

    // Šta bot trenutno "govori" tokom licitacije — čisti se na početku svake nove ruke
    private val botBidStatus: MutableMap<Int, BotBidStatus> = mutableMapOf()

    // Job za bot igranje — cancelujemo stari pre nego što startujemo novi
    private var botJob: Job? = null

    // Partija je gotova (zbir svih tabela je pao na 0 ili niže) — vidi finishHand/isGameOver
    private var gameOver: Boolean = false

    private val _state = MutableStateFlow(buildSnapshot(GameMessage.Loading))
    val state: StateFlow<GameSnapshot> = _state.asStateFlow()

    init {
        if (savedState != null) restoreFrom(savedState) else startNewHand(isFirstHand = true)
    }

    // ─── Javne metode — poziva ViewModel ─────────────────────────────────

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

    /** Snima kompletno interno stanje partije — za perzistenciju (Continue). */
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

    // ─── Vraćanje sačuvane partije (Continue) ────────────────────────────

    private fun restoreFrom(save: SavedGameState) {
        gameState.restoreDealerPosition(save.dealerPosition)
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
        save.wonTricksByPlayer.forEach { (pos, tricks) -> wonTricksByPlayer[pos] = tricks.toMutableList() }
        botBidStatus.clear()

        val firstPlayer = (save.dealerPosition + 1) % 3

        val finishedTricks = save.finishedTricks.map { cards ->
            Trick(save.trumpSuit).apply { cards.forEach { playCard(it.playerPosition, it.card) } }
        }
        val currentTrick = if (save.phase == HandPhase.PLAYING) {
            Trick(save.trumpSuit).apply { save.currentTrickCards.forEach { playCard(it.playerPosition, it.card) } }
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

    /**
     * Nakon vraćanja partije, nastavlja bilo koju automatsku (bot) akciju koja je bila u toku
     * u trenutku snimanja — npr. bot koji je trebalo da odigra kartu, licitira ili uzme kup.
     */
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

    // ─── Nova ruka ────────────────────────────────────────────────────────

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
            // Prva ruka partije — nema prethodne poruke koju treba ostaviti vidljivom,
            // pa nema razloga za čekanje (inače igrač gleda prazan "Loading" ekran uzalud).
            emit(GameMessage.NewRound(dealerName, firstAskedName))
            launchBotBidding()
        } else {
            // Poruka o kraju prethodne ruke (Runda gotova / Refa) ostaje vidljiva još
            // HAND_TRANSITION_DELAY_MS pre nego što se najavi nova runda — dok se ovo ne emituje,
            // _state i dalje pokazuje poslednju poruku, iako je nova ruka već interno spremna.
            botJob = scope.launch {
                delay(HAND_TRANSITION_DELAY_MS)
                emit(GameMessage.NewRound(dealerName, firstAskedName))
                launchBotBidding()
            }
        }
    }

    // ─── Licitiranje ──────────────────────────────────────────────────────

    private fun launchBotBidding() {
        botJob?.cancel()
        botJob = scope.launch { processBotBidding() }
    }

    /**
     * Botovi licitiraju jedan po jedan — svaki "razmišlja" [botThinkDelay] pre nego što kaže
     * "Dalje" (ili "Dobar", ako se pasira odmah nakon što je čovek licitirao minimalnih 2).
     */
    private suspend fun processBotBidding() {
        val bm = biddingManager!!
        while (!bm.isFinished() && bm.currentPlayer != HUMAN) {
            val botPos = bm.currentPlayer
            botBidStatus[botPos] = BotBidStatus.Thinking
            emit(GameMessage.BotAskedToBid(players[botPos].name))
            delay(botThinkDelay())

            val saysGood = bm.currentWinner == HUMAN && bm.currentHighestBid == 2
            bm.bid(botPos, BidAction.PASS)
            botBidStatus[botPos] = if (saysGood) BotBidStatus.Good else BotBidStatus.Pass
            emit(
                if (saysGood) GameMessage.BotSaysGood(players[botPos].name)
                else GameMessage.BotPasses(players[botPos].name)
            )
            delay(BOT_SAY_READ_DELAY_MS) // da igrač stigne da pročita šta je bot rekao
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

    // ─── Bot uzima kup i najavaljuje ──────────────────────────────────────

    /**
     * Bot pobedio licitaciju: prvo se kup otkriva svima (isto kao kad čovek uzima kup),
     * ostane vidljiv TALON_SHOW_DELAY_MS da igrač stigne da ga vidi, pa se tek onda
     * karte stavljaju u botovu ruku. Bot potom "razmišlja" pre bacanja 2 karte i pre
     * najave igre (svaka odluka ima sopstveni [botThinkDelay]).
     *
     * Funkcija je idempotentna u odnosu na fazu ruke — ako je partija vraćena iz sačuvanog
     * stanja usred ovog procesa (Continue), nastavlja tačno od koraka gde je stala umesto
     * da ponovo odradi već završene korake.
     */
    private suspend fun botHandleTalonAndDeclare(botPos: Int) {
        val hand = currentHand()
        val bot = players[botPos]

        if (hand.phase != HandPhase.DISCARDING && hand.phase != HandPhase.TRUMP_DECLARATION) {
            hand.moveToPhase(HandPhase.TALON_PICKUP)
            emit(GameMessage.BotTakesTalon(bot.name))
            delay(TALON_SHOW_DELAY_MS)

            bot.addCards(talon!!.cards)
            hand.moveToPhase(HandPhase.DISCARDING)
        }

        if (hand.phase == HandPhase.DISCARDING) {
            delay(botThinkDelay())
            val toDiscard = bot.hand.shuffled().take(2)
            toDiscard.forEach { bot.playCard(it) }
            hand.moveToPhase(HandPhase.TRUMP_DECLARATION)
        }

        delay(botThinkDelay())
        val gameType = currentGameType ?: BidType.fromValue(minimumBidValue) ?: BidType.SPADES
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
            delay(botThinkDelay())
            fm.decide(botPos, true)
            emit(GameMessage.BotFollows(players[botPos].name))
            delay(BOT_SAY_READ_DELAY_MS) // da igrač stigne da pročita šta je bot rekao
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

    /** Botovi igraju jedan po jedan, uz "razmišljanje" pre svakog poteza i pauzu za prikaz kompletnog stiha */
    private suspend fun playBotsSequentially() {
        val hand = currentHand()

        while (hand.phase == HandPhase.PLAYING && !hand.isFinished() && hand.currentPlayer != HUMAN) {
            delay(botThinkDelay()) // Bot "razmislja"

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

    /**
     * Proverava da li je partija gotova (zbir svih tabela poena pao je na 0 ili niže — neki
     * igrači su u minusu, neki u plusu). Ako jeste, zakazuje prikaz konačnog rezultata nakon
     * kratke pauze (da se poslednja poruka pročita) i vraća true — pozivalac ne sme da nastavi
     * na sledeću ruku.
     */
    private fun endGameIfOver(): Boolean {
        val sum = gameState.scoreSheets.values.sumOf { it.center }
        if (sum > 0) return false

        gameOver = true
        botJob?.cancel()
        botJob = scope.launch {
            delay(HAND_TRANSITION_DELAY_MS)
            emit(GameMessage.GameOver)
        }
        return true
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

    /** Nasumičan delay "razmišljanja" bota, u granicama [BOT_MIN_THINK_MS]..[BOT_MAX_THINK_MS]. */
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

        // Kup se vidi svima tokom TALON_PICKUP faze, bez obzira ko ga uzima — bot ga isto
        // "otkriva na sto" pre nego što ga stavi u ruku (vidi botHandleTalonAndDeclare)
        val talonVisible = hand?.phase == HandPhase.TALON_PICKUP
        // Dok se prikazuje upravo završen stih (showingCompletedTrick), niko ne sme da igra —
        // ni igrač koji je slučajno pobedio svoj sopstveni stih i odmah bi bio "na redu" opet,
        // jer bi to skratilo fiksno vreme prikaza kompletnog stiha
        val isHumanTurn = if (showingCompletedTrick) false else when (hand?.phase) {
            HandPhase.BIDDING -> bm?.currentPlayer == HUMAN
            HandPhase.TALON_PICKUP -> hand.declarer == HUMAN
            HandPhase.DISCARDING -> hand.declarer == HUMAN
            HandPhase.TRUMP_DECLARATION -> hand.declarer == HUMAN
            HandPhase.FOLLOW -> fm?.currentPlayer == HUMAN
            // hand.isFinished() štiti od trenutka kad je ruka odigrana do kraja ali se
            // tranzicija na novu rundu i dalje čeka (vidi HAND_TRANSITION_DELAY_MS u
            // startNewHand) — currentPlayer i dalje pokazuje poslednjeg pobednika stiha
            HandPhase.PLAYING -> !hand.isFinished() && hand.currentPlayer == HUMAN
            else -> false
        }

        // Legalne karte tokom PLAYING faze — za highlight u UI-u
        val legalCards = if (!showingCompletedTrick && hand?.phase == HandPhase.PLAYING && hand.currentPlayer == HUMAN) {
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

package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.model.Trick

class HandState(
    val players: List<Player>,
    firstPlayer: Int,
) {
    var phase: HandPhase = HandPhase.BIDDING
        private set
    var declarer: Int? = null
        private set
    var trumpSuit: Suit? = null
        private set

    var currentPlayer: Int = firstPlayer
        private set

    var currentTrick: Trick? = null
        private set

    private val _finishedTricks = mutableListOf<Trick>()
    val finishedTricks: List<Trick>
        get() = _finishedTricks

    fun setDeclarer(player: Int) {
        declarer = player
    }

    fun setTrumpSuit(suit: Suit) {
        require(phase == HandPhase.TRUMP_DECLARATION)
        trumpSuit = suit
    }

    fun moveToPhase(phase: HandPhase) {
        this.phase = phase
    }

    fun nextPlayer() {
        currentPlayer = (currentPlayer + 1) % 3
    }

    fun startPlayingPhase() {
        require(phase == HandPhase.FOLLOW)
        moveToPhase(HandPhase.PLAYING)
        currentTrick = Trick(trumpSuit)
    }

    fun finishCurrentTrick() {
        require(phase == HandPhase.PLAYING)
        _finishedTricks.add(currentTrick!!)
        currentTrick = Trick(trumpSuit)
    }

    fun setCurrentPlayer(player: Int) {
        currentPlayer = player
    }

    fun isFinished(): Boolean {
        return phase == HandPhase.FINISHED || finishedTricks.size == 10
    }
}
package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.model.Trick

class RoundState(
    val players: List<Player>,
    val trumpSuit: Suit,
    firstPlayer: Int,
    val talon: Talon
) {
    var currentPlayer: Int = firstPlayer
        private set

    var currentTrick: Trick = Trick(trumpSuit)
        private set

    private val _finishedTricks = mutableListOf<Trick>()
    val finishedTricks: List<Trick>
        get() = _finishedTricks

    fun nextPlayer() {
        currentPlayer = (currentPlayer + 1) % 3
    }

    fun finishCurrentTrick() {
        _finishedTricks.add(currentTrick)
        currentTrick = Trick(trumpSuit)
    }

    fun setCurrentPlayer(player : Int){
        currentPlayer = player
    }
}
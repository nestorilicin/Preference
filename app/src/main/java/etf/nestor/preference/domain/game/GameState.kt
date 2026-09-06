package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.scoring.ScoreSheet

class GameState(val players: List<Player>, start: Int) {
    var dealerPosition: Int = 0
        private set
    var currentHand: HandState? = null
        private set
    val scoreSheets = mutableMapOf(
        0 to ScoreSheet(start), 1 to ScoreSheet(start), 2 to ScoreSheet(start)
    )

    fun startHand(handState: HandState) {
        currentHand = handState
    }

    fun finishHand() {
        currentHand = null
        dealerPosition = (dealerPosition + 1) % 3
    }

    internal fun setDealerPosition(position: Int) {
        dealerPosition = position
    }

}

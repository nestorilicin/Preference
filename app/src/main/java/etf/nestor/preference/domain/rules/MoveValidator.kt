package etf.nestor.preference.domain.rules

import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.model.Trick

object MoveValidator {
    fun isValidMove(playerHand: List<PlayingCard>, cardToPlay: PlayingCard, trick: Trick): Boolean {
        if (trick.isEmpty()) return true
        val leadSuit = trick.leadSuit!!
        val hasLeadSuit = playerHand.any { it.suit == leadSuit }
        val trumpSuit = trick.trumpSuit
        val hasTrumpSuit = playerHand.any { it.suit == trumpSuit }
        if (!hasLeadSuit) {
            if (trumpSuit != Suit.NO_SUIT && hasTrumpSuit) return cardToPlay.suit == trumpSuit
            return true
        }
        return cardToPlay.suit == leadSuit
    }
}
package etf.nestor.preference.domain.rules

import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.Trick

object TrickResolver {
    fun resolveWinner(trick: Trick): PlayedCard {
        require(trick.playedCards.isNotEmpty()) {
            "No cards error"
        }
        val leadSuit = trick.leadSuit!!
        val trumpSuit = trick.trumpSuit
        return trick.playedCards.maxBy { playedCard ->
            val card = playedCard.card
            when (card.suit) {
                trumpSuit -> card.rank.power + 200
                leadSuit -> card.rank.power + 100
                else -> card.rank.power
            }
        }
    }
}
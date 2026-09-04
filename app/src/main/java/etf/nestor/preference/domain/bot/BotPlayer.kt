package etf.nestor.preference.domain.bot

import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Trick
import etf.nestor.preference.domain.rules.MoveValidator

object BotPlayer {
    fun decideCard(hand: List<PlayingCard>, trick: Trick): PlayingCard {
        val legalCards = hand.filter { MoveValidator.isValidMove(hand, it, trick) }
        return legalCards.random()
    }
}

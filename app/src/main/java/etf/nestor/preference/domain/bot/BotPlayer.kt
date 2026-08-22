package etf.nestor.preference.domain.bot

import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Trick
import etf.nestor.preference.domain.rules.MoveValidator

/**
 * Jednostavan bot:
 * - Licitiranje: uvek kaze "Dalje" (PASS) — logika u fasadi
 * - Pracenje: uvek prati — logika u fasadi
 * - Igranje: igra nasumicnu legalnu kartu
 */
object BotPlayer {
    fun decideCard(hand: List<PlayingCard>, trick: Trick): PlayingCard {
        val legalCards = hand.filter { MoveValidator.isValidMove(hand, it, trick) }
        return legalCards.random()
    }
}

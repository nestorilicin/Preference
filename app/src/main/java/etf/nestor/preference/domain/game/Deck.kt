package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit

class Deck {
    private val cards = mutableListOf<PlayingCard>()

    init {
        reset()
    }

    fun reset() {
        cards.clear()
        Suit.entries.filter { it != Suit.NO_SUIT }.forEach { suit ->
            Rank.entries.forEach { rank ->
                cards.add(PlayingCard(suit, rank))
            }
        }
    }

    fun shuffle() {
        cards.shuffle()
    }

    fun draw(): PlayingCard {
        require(cards.isNotEmpty()) { "Empty deck" }
        return cards.removeAt(0)
    }

    fun remainingCards(): Int {
        return cards.size
    }
}
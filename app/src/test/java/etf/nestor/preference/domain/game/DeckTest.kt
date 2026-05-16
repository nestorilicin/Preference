package etf.nestor.preference.domain.game

import org.junit.Test

class DeckTest {
    @Test
    fun deck_has_32_cards() {
        val deck = Deck()
        assert(deck.remainingCards() == 32)
    }

    @Test
    fun drawing_card_reduces_size() {
        val deck = Deck()
        deck.draw()
        assert(deck.remainingCards() == 31)
    }

    @Test
    fun reset_restores_deck() {
        val deck = Deck()
        deck.draw()
        deck.draw()
        deck.reset()
        assert(deck.remainingCards() == 32)
    }
}
package etf.nestor.preference.domain.game

import org.junit.Test

class DealerTest {

    @Test
    fun dealer_distributes_correct_number_of_cards() {

        val deck = Deck()

        val result = Dealer.deal(deck)

        val player0Cards =
            result.playersCards[0]

        val player1Cards =
            result.playersCards[1]

        val player2Cards =
            result.playersCards[2]

        val talonCards =
            result.talon.cards

        assert(player0Cards.size == 10)

        assert(player1Cards.size == 10)

        assert(player2Cards.size == 10)

        assert(talonCards.size == 2)
    }

    @Test
    fun deck_should_be_empty_after_dealing() {

        val deck = Deck()

        Dealer.deal(deck)

        assert(
            deck.remainingCards() == 0
        )
    }

    @Test
    fun all_cards_should_be_unique_after_dealing() {

        val deck = Deck()

        val result = Dealer.deal(deck)

        val allCards = mutableListOf<Any>()

        allCards.addAll(
            result.playersCards[0]
        )

        allCards.addAll(
            result.playersCards[1]
        )

        allCards.addAll(
            result.playersCards[2]
        )

        allCards.addAll(
            result.talon.cards
        )

        assert(
            allCards.size == 32
        )

        assert(
            allCards.distinct().size == 32
        )
    }
}
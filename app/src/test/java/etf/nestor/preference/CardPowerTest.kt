package etf.nestor.preference

import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.model.TrickContext
import etf.nestor.preference.domain.model.findStrongestCard
import org.junit.Test

class CardPowerTest {
    @Test
    fun test_with_3_cards_and_trump() {
        val ctx = TrickContext(Suit.SPADES, Suit.HEARTS)
        val cards = listOf(
            PlayingCard(Suit.SPADES, Rank.KING),
            PlayingCard(Suit.SPADES, Rank.ACE),
            PlayingCard(Suit.HEARTS, Rank.SEVEN)
        )
        val winner = findStrongestCard(cards, ctx)
        assert(winner == PlayingCard(Suit.HEARTS, Rank.SEVEN))
    }
    @Test
    fun test_with_3_cards_no_trump(){
        val ctx = TrickContext(Suit.SPADES, Suit.HEARTS)
        val cards = listOf(
            PlayingCard(Suit.SPADES, Rank.QUEEN),
            PlayingCard(Suit.SPADES, Rank.JACK),
            PlayingCard(Suit.SPADES, Rank.SEVEN)
        )
        val winner = findStrongestCard(cards, ctx)
        assert(winner == PlayingCard(Suit.SPADES, Rank.QUEEN))
    }
    @Test
    fun test_with_2_cards_and_trump(){
        val ctx = TrickContext(Suit.CLUBS, Suit.HEARTS)
        val cards = listOf(
            PlayingCard(Suit.CLUBS, Rank.ACE),
            PlayingCard(Suit.HEARTS, Rank.ACE),
        )
        val winner = findStrongestCard(cards, ctx)
        assert(winner == PlayingCard(Suit.HEARTS, Rank.ACE))
    }
    @Test
    fun test_with_3_different_suits(){
        val ctx = TrickContext(Suit.SPADES, Suit.DIAMONDS)
        val cards = listOf(
            PlayingCard(Suit.HEARTS, Rank.ACE),
            PlayingCard(Suit.SPADES, Rank.ACE),
            PlayingCard(Suit.CLUBS, Rank.ACE)
        )
        val winner = findStrongestCard(cards, ctx)
        assert(winner == PlayingCard(Suit.SPADES, Rank.ACE))
    }
    @Test
    fun test_with_no_suit(){
        val ctx = TrickContext(Suit.NO_SUIT, Suit.NO_SUIT)
        val cards = listOf(
            PlayingCard(Suit.SPADES, Rank.EIGHT),
            PlayingCard(Suit.SPADES, Rank.NINE),
            PlayingCard(Suit.SPADES, Rank.TEN)
        )
        val winner = findStrongestCard(cards, ctx)
        assert(winner == PlayingCard(Suit.SPADES, Rank.TEN))
    }
}
package etf.nestor.preference

import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.model.Trick
import etf.nestor.preference.domain.rules.TrickResolver
import org.junit.Test

class TrickResolverTest {
    @Test
    fun trump_wins_over_ace() {
        val trick = Trick(trumpSuit = Suit.HEARTS)
        trick.playCard(0, PlayingCard(Suit.SPADES, Rank.ACE))
        trick.playCard(1, PlayingCard(Suit.HEARTS, Rank.SEVEN))
        trick.playCard(2, PlayingCard(Suit.SPADES, Rank.KING))
        val winner = TrickResolver.resolveWinner(trick)
        assert(winner.playerPosition == 1)
    }

    @Test
    fun highest_lead_suit_wins() {
        val trick = Trick(trumpSuit = Suit.HEARTS)
        trick.playCard(0, PlayingCard(Suit.SPADES, Rank.QUEEN))
        trick.playCard(1, PlayingCard(Suit.SPADES, Rank.ACE))
        trick.playCard(2, PlayingCard(Suit.CLUBS, Rank.ACE))
        val winner = TrickResolver.resolveWinner(trick)
        assert(winner.playerPosition == 1)
    }

    @Test
    fun highest_trump_wins() {
        val trick = Trick(trumpSuit = Suit.HEARTS)
        trick.playCard(0, PlayingCard(Suit.HEARTS, Rank.SEVEN))
        trick.playCard(1, PlayingCard(Suit.HEARTS, Rank.ACE))
        trick.playCard(2, PlayingCard(Suit.SPADES, Rank.ACE))
        val winner = TrickResolver.resolveWinner(trick)
        assert(winner.playerPosition == 1)
    }

    @Test
    fun misere_test(){
        val trick = Trick(trumpSuit = Suit.NO_SUIT)
        trick.playCard(1, PlayingCard(Suit.HEARTS, Rank.SEVEN))
        trick.playCard(2, PlayingCard(Suit.HEARTS, Rank.EIGHT))
        trick.playCard(0, PlayingCard(Suit.DIAMONDS, Rank.ACE))
        val winner = TrickResolver.resolveWinner(trick)
        assert(winner.playerPosition == 2)
    }
}
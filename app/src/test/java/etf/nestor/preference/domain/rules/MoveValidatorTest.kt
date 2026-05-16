package etf.nestor.preference.domain.rules

import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.model.Trick
import etf.nestor.preference.domain.rules.MoveValidator
import org.junit.Test

class MoveValidatorTest {
    @Test
    fun player_must_follow_lead() {
        val trick = Trick(Suit.HEARTS)

        trick.playCard(0, PlayingCard(Suit.SPADES, Rank.ACE))

        val hand = listOf(
            PlayingCard(Suit.SPADES, Rank.KING), PlayingCard(Suit.HEARTS, Rank.ACE)
        )

        val result = MoveValidator.isValidMove(
            hand, PlayingCard(Suit.HEARTS, Rank.ACE), trick
        )

        assert(!result)
    }

    @Test
    fun player_must_trump_if_no_lead() {
        val trick = Trick(Suit.HEARTS)

        trick.playCard(0, PlayingCard(Suit.DIAMONDS, Rank.ACE))
        val hand = listOf(
            PlayingCard(Suit.SPADES, Rank.KING), PlayingCard(Suit.HEARTS, Rank.ACE)
        )

        val result = MoveValidator.isValidMove(
            hand, PlayingCard(Suit.HEARTS, Rank.ACE), trick
        )

        assert(result)
    }

    @Test
    fun player_must_can_play_anything_if_no_trump_and_no_lead() {
        val trick = Trick(Suit.HEARTS)
        trick.playCard(0, PlayingCard(Suit.SPADES, Rank.ACE))
        val hand = listOf(
            PlayingCard(Suit.CLUBS, Rank.KING), PlayingCard(Suit.DIAMONDS, Rank.QUEEN)
        )
        val result = MoveValidator.isValidMove(
            hand, PlayingCard(Suit.CLUBS, Rank.KING), trick
        )
        assert(result)
    }

    @Test
    fun misere_test() {
        val trick = Trick(
            Suit.NO_SUIT
        )
        trick.playCard(
            0, PlayingCard(Suit.SPADES, Rank.ACE)
        )
        val hand = listOf(
            PlayingCard(Suit.HEARTS, Rank.KING), PlayingCard(Suit.CLUBS, Rank.QUEEN)
        )
        val result = hand.all { card -> MoveValidator.isValidMove(hand, card, trick) }
        assert(result)
    }

    @Test
    fun player_can_play_anything_on_empty_trick() {
        val trick = Trick(Suit.SPADES)
        val hand = listOf(
            PlayingCard(Suit.HEARTS, Rank.KING),
            PlayingCard(Suit.CLUBS, Rank.QUEEN),
            PlayingCard(Suit.DIAMONDS, Rank.SEVEN),
            PlayingCard(Suit.DIAMONDS, Rank.EIGHT),
            PlayingCard(Suit.DIAMONDS, Rank.NINE),
            PlayingCard(Suit.DIAMONDS, Rank.TEN),
            PlayingCard(Suit.SPADES, Rank.ACE)
        )
        val result = hand.all { card -> MoveValidator.isValidMove(hand, card, trick) }
        assert(result)
    }
}
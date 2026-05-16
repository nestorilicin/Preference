package etf.nestor.preference.domain.model

import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit
import org.junit.Test

class PlayerTest {
    @Test
    fun player_add_card() {
        val player = Player("Nestor", 120, 0)
        val card = PlayingCard(Suit.SPADES, Rank.ACE)
        player.addCard(card)
        assert(player.hand.contains(card))
    }

    @Test
    fun player_can_play_owned_card() {
        val player = Player("Nestor", 120, 1)
        val card = PlayingCard(Suit.HEARTS, Rank.KING)
        player.addCard(card)
        val played = player.playCard(card)
        assert(played == card)
        assert(!player.hand.contains(card))
    }

    @Test(expected = IllegalArgumentException::class)
    fun player_cannot_play_card_he_does_not_have() {
        val player = Player("Nestor", 120, 2)
        val card = PlayingCard(Suit.CLUBS, Rank.ACE)
        player.playCard(card)
    }

    @Test
    fun player_trick_counter_increases() {
        val player = Player("Nestor", 120, 0)
        player.wonTrick()
        player.wonTrick()
        assert(player.tricksWon == 2)
    }

    @Test
    fun player_increase_points(){
        val player = Player("Igrac", 120, 0)
        player.gainPoints(-10)
        assert(player.totalPoints == 110)
    }
    @Test
    fun player_add_cards_and_clean(){
        val player = Player("Testic", 100, 0)
        player.addCards(listOf(
            PlayingCard(Suit.SPADES, Rank.TEN),
            PlayingCard(Suit.SPADES, Rank.EIGHT),
            PlayingCard(Suit.SPADES, Rank.ACE),
            PlayingCard(Suit.DIAMONDS, Rank.ACE),
            PlayingCard(Suit.CLUBS, Rank.EIGHT),
            PlayingCard(Suit.HEARTS, Rank.NINE),
            PlayingCard(Suit.DIAMONDS, Rank.NINE),
            PlayingCard(Suit.HEARTS, Rank.ACE),
            PlayingCard(Suit.CLUBS, Rank.QUEEN),
            PlayingCard(Suit.DIAMONDS, Rank.JACK)
        ))
        assert(player.handSize() == 10)
        player.clearHand()
        assert(player.handSize() == 0)
    }
}
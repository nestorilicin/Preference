package etf.nestor.preference

import etf.nestor.preference.domain.game.Dealer
import etf.nestor.preference.domain.game.Deck
import etf.nestor.preference.domain.game.GameEngine
import etf.nestor.preference.domain.game.RoundState
import etf.nestor.preference.domain.game.Talon
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit
import org.junit.Test

class GamePackageTest {
    private fun createPlayers(): List<Player> {
        return listOf(
            Player(
                "Player0", 120, 0
            ), Player(
                "Player1", 120, 1
            ), Player(
                "Player2", 120, 2
            )
        )
    }

    @Test
    fun round_state_initializes_correctly() {
        val roundState = RoundState(
            players = createPlayers(),
            trumpSuit = Suit.HEARTS,
            firstPlayer = 1,
            talon = Talon(mutableListOf())
        )
        assert(roundState.currentPlayer == 1)
        assert(roundState.trumpSuit == Suit.HEARTS)
        assert(roundState.finishedTricks.isEmpty())
        assert(roundState.currentTrick.playedCards.isEmpty())
    }

    @Test
    fun next_player_rotates_correctly() {
        val roundState = RoundState(
            players = createPlayers(),
            trumpSuit = Suit.SPADES,
            firstPlayer = 0,
            talon = Talon(mutableListOf())
        )
        roundState.nextPlayer()
        assert(roundState.currentPlayer == 1)
        roundState.nextPlayer()
        assert(roundState.currentPlayer == 2)
        roundState.nextPlayer()
        assert(roundState.currentPlayer == 0)
    }

    @Test
    fun finished_trick_is_added_to_history() {
        val roundState = RoundState(
            players = createPlayers(),
            trumpSuit = Suit.CLUBS,
            firstPlayer = 0,
            talon = Talon(mutableListOf())
        )
        val oldTrick = roundState.currentTrick
        roundState.finishCurrentTrick()
        assert(roundState.finishedTricks.size == 1)
        assert(roundState.finishedTricks[0] == oldTrick)
    }

    @Test
    fun finishing_trick_creates_new_current_trick() {
        val roundState = RoundState(
            players = createPlayers(),
            trumpSuit = Suit.DIAMONDS,
            firstPlayer = 2,
            talon = Talon(mutableListOf())
        )
        val oldTrick = roundState.currentTrick
        roundState.finishCurrentTrick()
        assert(roundState.currentTrick != oldTrick)
    }

    @Test
    fun new_trick_keeps_same_trump_suit() {
        val roundState = RoundState(
            players = createPlayers(),
            trumpSuit = Suit.HEARTS,
            firstPlayer = 0,
            talon = Talon(mutableListOf())
        )
        roundState.finishCurrentTrick()
        assert(roundState.currentTrick.trumpSuit == Suit.HEARTS)
    }

    @Test
    fun full_trick_simulation() {

        val players = createPlayers()

        players[0].addCard(
            PlayingCard(Suit.SPADES, Rank.ACE)
        )

        players[1].addCard(
            PlayingCard(Suit.HEARTS, Rank.SEVEN)
        )

        players[2].addCard(
            PlayingCard(Suit.SPADES, Rank.KING)
        )

        val roundState = RoundState(
            players = players,
            trumpSuit = Suit.HEARTS,
            talon = Talon(mutableListOf()),
            firstPlayer = 0
        )

        val engine = GameEngine(
            roundState
        )

        engine.playTurn(
            PlayedCard(
                0, PlayingCard(Suit.SPADES, Rank.ACE)
            )
        )

        engine.playTurn(
            PlayedCard(
                1, PlayingCard(Suit.HEARTS, Rank.SEVEN)
            )
        )

        engine.playTurn(
            PlayedCard(
                2, PlayingCard(Suit.SPADES, Rank.KING)
            )
        )

        assert(
            roundState.currentPlayer == 1
        )

        assert(
            players[1].tricksWon == 1
        )

        assert(
            roundState.finishedTricks.size == 1
        )
    }

    @Test
    fun dealer_test() {

        val deck = Deck()

        val result = Dealer.deal(deck)

        val player0Cards = result.playersCards[0]
        val player1Cards = result.playersCards[1]
        val player2Cards = result.playersCards[2]

        val talonCards = result.talon.cards

        assert(player0Cards.size == 10)
        assert(player1Cards.size == 10)
        assert(player2Cards.size == 10)

        assert(talonCards.size == 2)

        assert(deck.remainingCards() == 0)

        val allCards = mutableListOf<Any>()

        allCards.addAll(player0Cards)
        allCards.addAll(player1Cards)
        allCards.addAll(player2Cards)
        allCards.addAll(talonCards)

        assert(allCards.size == 32)

        assert(allCards.distinct().size == 32)
    }
}

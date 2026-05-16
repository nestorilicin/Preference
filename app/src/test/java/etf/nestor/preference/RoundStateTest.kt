package etf.nestor.preference

import etf.nestor.preference.domain.game.RoundState
import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.Suit
import org.junit.Test

class RoundStateTest {
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
            players = createPlayers(), trumpSuit = Suit.HEARTS, firstPlayer = 1
        )
        assert(roundState.currentPlayer == 1)
        assert(roundState.trumpSuit == Suit.HEARTS)
        assert(roundState.finishedTricks.isEmpty())
        assert(roundState.currentTrick.playedCards.isEmpty())
    }

    @Test
    fun next_player_rotates_correctly() {
        val roundState = RoundState(
            players = createPlayers(), trumpSuit = Suit.SPADES, firstPlayer = 0
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
            players = createPlayers(), trumpSuit = Suit.CLUBS, firstPlayer = 0
        )
        val oldTrick = roundState.currentTrick
        roundState.finishCurrentTrick()
        assert(roundState.finishedTricks.size == 1)
        assert(roundState.finishedTricks[0] == oldTrick)
    }

    @Test
    fun finishing_trick_creates_new_current_trick() {
        val roundState = RoundState(
            players = createPlayers(), trumpSuit = Suit.DIAMONDS, firstPlayer = 2
        )
        val oldTrick = roundState.currentTrick
        roundState.finishCurrentTrick()
        assert(roundState.currentTrick != oldTrick)
    }

    @Test
    fun new_trick_keeps_same_trump_suit() {
        val roundState = RoundState(
            players = createPlayers(), trumpSuit = Suit.HEARTS, firstPlayer = 0
        )
        roundState.finishCurrentTrick()
        assert(roundState.currentTrick.trumpSuit == Suit.HEARTS)
    }
}
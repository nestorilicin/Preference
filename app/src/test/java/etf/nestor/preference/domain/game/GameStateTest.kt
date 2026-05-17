package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.Player
import org.junit.Assert.*
import org.junit.Test

class GameStateTest {

    private fun createPlayers(): List<Player> {
        return listOf(
            Player("P1", 0),
            Player("P2", 1),
            Player("P3",  2)
        )
    }

    @Test
    fun initial_dealer_position_should_be_zero() {
        val gameState = GameState(
            players = createPlayers(),
            start = 10
        )

        assertEquals(
            0,
            gameState.dealerPosition
        )
    }

    @Test
    fun current_hand_should_initially_be_null() {
        val gameState = GameState(
            players = createPlayers(),
            start = 10
        )

        assertNull(
            gameState.currentHand
        )
    }

    @Test
    fun score_sheets_should_exist_for_all_players() {
        val gameState = GameState(
            players = createPlayers(),
            start = 15
        )

        assertEquals(
            3,
            gameState.scoreSheets.size
        )
    }

    @Test
    fun score_sheets_should_contain_player_zero() {
        val gameState = GameState(
            players = createPlayers(),
            start = 15
        )

        assertTrue(
            gameState.scoreSheets.containsKey(0)
        )
    }

    @Test
    fun score_sheets_should_contain_player_one() {
        val gameState = GameState(
            players = createPlayers(),
            start = 15
        )

        assertTrue(
            gameState.scoreSheets.containsKey(1)
        )
    }

    @Test
    fun score_sheets_should_contain_player_two() {
        val gameState = GameState(
            players = createPlayers(),
            start = 15
        )

        assertTrue(
            gameState.scoreSheets.containsKey(2)
        )
    }

    @Test
    fun start_hand_should_set_current_hand() {
        val gameState = GameState(
            players = createPlayers(),
            start = 10
        )

        val handState = HandState(
            players = gameState.players,
            firstPlayer = 1
        )

        gameState.startHand(handState)

        assertEquals(
            handState,
            gameState.currentHand
        )
    }

    @Test
    fun finish_hand_should_clear_current_hand() {
        val gameState = GameState(
            players = createPlayers(),
            start = 10
        )

        val handState = HandState(
            players = gameState.players,
            firstPlayer = 1
        )

        gameState.startHand(handState)

        gameState.finishHand()

        assertNull(
            gameState.currentHand
        )
    }

    @Test
    fun finish_hand_should_rotate_dealer_position() {
        val gameState = GameState(
            players = createPlayers(),
            start = 10
        )

        gameState.finishHand()

        assertEquals(
            1,
            gameState.dealerPosition
        )
    }

    @Test
    fun dealer_position_should_wrap_after_three_rotations() {
        val gameState = GameState(
            players = createPlayers(),
            start = 10
        )

        gameState.finishHand()
        gameState.finishHand()
        gameState.finishHand()

        assertEquals(
            0,
            gameState.dealerPosition
        )
    }

    @Test
    fun start_hand_should_replace_previous_hand() {
        val gameState = GameState(
            players = createPlayers(),
            start = 10
        )

        val firstHand = HandState(
            players = gameState.players,
            firstPlayer = 0
        )

        val secondHand = HandState(
            players = gameState.players,
            firstPlayer = 2
        )

        gameState.startHand(firstHand)
        gameState.startHand(secondHand)

        assertEquals(
            secondHand,
            gameState.currentHand
        )
    }

    @Test
    fun players_should_be_preserved() {
        val players = createPlayers()

        val gameState = GameState(
            players = players,
            start = 10
        )

        assertEquals(
            players,
            gameState.players
        )
    }

    @Test
    fun multiple_finish_hand_calls_should_rotate_dealer() {
        val gameState = GameState(
            players = createPlayers(),
            start = 10
        )

        gameState.finishHand()
        assertEquals(1, gameState.dealerPosition)

        gameState.finishHand()
        assertEquals(2, gameState.dealerPosition)

        gameState.finishHand()
        assertEquals(0, gameState.dealerPosition)

        gameState.finishHand()
        assertEquals(1, gameState.dealerPosition)
    }
}
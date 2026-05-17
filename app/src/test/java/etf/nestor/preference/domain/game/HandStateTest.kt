package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.Suit
import org.junit.Test

class HandStateTest {

    private fun createPlayers(): List<Player> {
        return listOf(
            Player("Player0", 0), Player("Player1",  1), Player("Player2",  2)
        )
    }

    @Test
    fun hand_state_initializes_correctly() {

        val handState = HandState(
            players = createPlayers(), firstPlayer = 1
        )

        assert(handState.currentPlayer == 1)

        assert(handState.phase == HandPhase.BIDDING)

        assert(handState.trumpSuit == null)

        assert(handState.currentTrick == null)

        assert(handState.finishedTricks.isEmpty())
    }

    @Test
    fun next_player_rotates_correctly() {

        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )

        handState.nextPlayer()
        assert(handState.currentPlayer == 1)

        handState.nextPlayer()
        assert(handState.currentPlayer == 2)

        handState.nextPlayer()
        assert(handState.currentPlayer == 0)
    }

    @Test
    fun declarer_is_set_correctly() {

        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )

        handState.setDeclarer(2)

        assert(handState.declarer == 2)
    }

    @Test
    fun phase_changes_correctly() {

        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )

        handState.moveToPhase(
            HandPhase.TRUMP_DECLARATION
        )

        assert(
            handState.phase == HandPhase.TRUMP_DECLARATION
        )
    }

    @Test
    fun trump_is_set_correctly() {

        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )

        handState.moveToPhase(
            HandPhase.TRUMP_DECLARATION
        )

        handState.setTrumpSuit(
            Suit.HEARTS
        )

        assert(
            handState.trumpSuit == Suit.HEARTS
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun trump_cannot_be_set_in_wrong_phase() {

        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )

        handState.setTrumpSuit(
            Suit.CLUBS
        )
    }

    @Test
    fun playing_phase_creates_current_trick() {

        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )

        handState.moveToPhase(
            HandPhase.TRUMP_DECLARATION
        )

        handState.setTrumpSuit(
            Suit.SPADES
        )

        handState.moveToPhase(
            HandPhase.FOLLOW
        )

        handState.startPlayingPhase()

        assert(
            handState.phase == HandPhase.PLAYING
        )

        assert(
            handState.currentTrick != null
        )
    }

    @Test
    fun finishing_trick_adds_it_to_history() {

        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )

        handState.moveToPhase(
            HandPhase.TRUMP_DECLARATION
        )

        handState.setTrumpSuit(
            Suit.DIAMONDS
        )

        handState.moveToPhase(
            HandPhase.FOLLOW
        )

        handState.startPlayingPhase()

        val oldTrick = handState.currentTrick

        handState.finishCurrentTrick()

        assert(
            handState.finishedTricks.size == 1
        )

        assert(
            handState.finishedTricks[0] == oldTrick
        )
    }
    @Test
    fun finishing_trick_creates_new_trick() {
        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )
        handState.moveToPhase(
            HandPhase.TRUMP_DECLARATION
        )
        handState.setTrumpSuit(
            Suit.HEARTS
        )
        handState.moveToPhase(
            HandPhase.FOLLOW
        )
        handState.startPlayingPhase()
        val oldTrick = handState.currentTrick
        handState.finishCurrentTrick()
        assert(
            handState.currentTrick != oldTrick
        )
    }

    @Test
    fun is_finished_returns_true_when_phase_is_finished() {
        val handState = HandState(
            players = createPlayers(), firstPlayer = 0
        )
        handState.moveToPhase(
            HandPhase.FINISHED
        )
        assert(
            handState.isFinished()
        )
    }
}
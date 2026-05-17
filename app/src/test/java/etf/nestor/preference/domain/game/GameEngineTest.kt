package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.Player
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit
import org.junit.Test

class GameEngineTest {

    private fun createPlayers(): List<Player> {
        return listOf(
            Player("Player0", 0),
            Player("Player1",  1),
            Player("Player2",  2)
        )
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

        val handState = HandState(
            players = players,
            firstPlayer = 0
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

        val engine = GameEngine(
            handState
        )

        engine.playTurn(
            PlayedCard(
                0,
                PlayingCard(Suit.SPADES, Rank.ACE)
            )
        )

        engine.playTurn(
            PlayedCard(
                1,
                PlayingCard(Suit.HEARTS, Rank.SEVEN)
            )
        )

        engine.playTurn(
            PlayedCard(
                2,
                PlayingCard(Suit.SPADES, Rank.KING)
            )
        )

        assert(
            handState.currentPlayer == 1
        )

        assert(
            players[1].tricksWon == 1
        )

        assert(
            handState.finishedTricks.size == 1
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun player_cannot_play_out_of_turn() {

        val players = createPlayers()

        players[0].addCard(
            PlayingCard(Suit.SPADES, Rank.ACE)
        )

        val handState = HandState(
            players = players,
            firstPlayer = 0
        )

        handState.moveToPhase(
            HandPhase.TRUMP_DECLARATION
        )

        handState.setTrumpSuit(
            Suit.CLUBS
        )

        handState.moveToPhase(
            HandPhase.FOLLOW
        )

        handState.startPlayingPhase()

        val engine = GameEngine(
            handState
        )

        engine.playTurn(
            PlayedCard(
                1,
                PlayingCard(Suit.SPADES, Rank.ACE)
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun player_cannot_play_card_not_in_hand() {

        val players = createPlayers()

        val handState = HandState(
            players = players,
            firstPlayer = 0
        )

        handState.moveToPhase(
            HandPhase.TRUMP_DECLARATION
        )

        handState.setTrumpSuit(
            Suit.CLUBS
        )

        handState.moveToPhase(
            HandPhase.FOLLOW
        )

        handState.startPlayingPhase()

        val engine = GameEngine(
            handState
        )

        engine.playTurn(
            PlayedCard(
                0,
                PlayingCard(Suit.SPADES, Rank.ACE)
            )
        )
    }
}
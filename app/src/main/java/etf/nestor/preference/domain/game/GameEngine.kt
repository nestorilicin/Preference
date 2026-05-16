package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.rules.MoveValidator
import etf.nestor.preference.domain.rules.TrickResolver

class GameEngine(private val roundState: RoundState) {
    fun playTurn(playedCard: PlayedCard) {
        require(
            playedCard.playerPosition == roundState.currentPlayer
        ) {
            "Not this player's turn"
        }
        val player = roundState.players[playedCard.playerPosition]
        val isValid = MoveValidator.isValidMove(
            player.hand, playedCard.card, roundState.currentTrick
        )
        require(isValid) {
            "Invalid move"
        }
        player.playCard(
            playedCard.card
        )
        roundState.currentTrick.playCard(playedCard.playerPosition, playedCard.card)
        if (roundState.currentTrick.playedCards.size == 3) {
            val winner = TrickResolver.resolveWinner(
                roundState.currentTrick
            )
            roundState.players[winner.playerPosition].wonTrick()
            roundState.finishCurrentTrick()
            roundState.setCurrentPlayer(
                winner.playerPosition
            )
        } else {
            roundState.nextPlayer()
        }
    }
}
package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.rules.MoveValidator
import etf.nestor.preference.domain.rules.TrickResolver

class GameEngine(private val handState: HandState) {
    fun playTurn(playedCard: PlayedCard) {
        require(
            playedCard.playerPosition == handState.currentPlayer
        ) {
            "Not this player's turn"
        }
        val player = handState.players[playedCard.playerPosition]
        val isValid = MoveValidator.isValidMove(
            player.hand, playedCard.card, handState.currentTrick!!
        )
        require(isValid) {
            "Invalid move"
        }
        player.playCard(
            playedCard.card
        )
        handState.currentTrick!!.playCard(playedCard.playerPosition, playedCard.card)
        if (handState.currentTrick!!.playedCards.size == 3) {
            val winner = TrickResolver.resolveWinner(
                handState.currentTrick!!
            )
            handState.players[winner.playerPosition].wonTrick()
            handState.finishCurrentTrick()
            handState.setCurrentPlayer(
                winner.playerPosition
            )
        } else {
            handState.nextPlayer()
        }
    }
}
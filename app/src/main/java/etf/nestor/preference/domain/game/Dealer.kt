package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.PlayingCard

object Dealer {
    fun deal(deck : Deck): DealResult {
        deck.shuffle()
        val player0 = mutableListOf<PlayingCard>()
        val player1 = mutableListOf<PlayingCard>()
        val player2 = mutableListOf<PlayingCard>()
        repeat(5){
            player0.add(deck.draw())
            player1.add(deck.draw())
            player2.add(deck.draw())
        }
        val talon = Talon(mutableListOf(deck.draw(), deck.draw()))
        repeat(5){
            player0.add(deck.draw())
            player1.add(deck.draw())
            player2.add(deck.draw())
        }
        return DealResult(listOf(player0, player1, player2), talon)
    }
}
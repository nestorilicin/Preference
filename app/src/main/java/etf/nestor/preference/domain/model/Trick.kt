package etf.nestor.preference.domain.model



class Trick(val trumpSuit: Suit?) {
    private val _playedCards = mutableListOf<PlayedCard>()
    val playedCards: List<PlayedCard>
        get() = _playedCards
    val leadSuit: Suit?
        get() = _playedCards.firstOrNull()?.card?.suit
    fun playCard(playerPosition: Int, card: PlayingCard){
        _playedCards.add(PlayedCard(playerPosition, card))
    }
    fun isEmpty() : Boolean {
        return _playedCards.isEmpty()
    }
}
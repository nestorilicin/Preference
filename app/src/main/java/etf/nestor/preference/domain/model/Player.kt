package etf.nestor.preference.domain.model

class Player(val name: String, val position: Int) {
    var tricksWon: Int = 0

    private val _hand = mutableListOf<PlayingCard>()
    val hand: List<PlayingCard>
        get() = _hand

    fun addCard(card: PlayingCard) {
        _hand.add(card)
    }

    fun addCards(cards: List<PlayingCard>) {
        _hand.addAll(cards)
    }

    fun clearHand() {
        _hand.clear()
    }

    fun handSize(): Int {
        return _hand.size
    }

    fun playCard(card: PlayingCard): PlayingCard {
        require(_hand.contains(card)) {
            "Player doesn't have card"
        }
        _hand.remove(card)
        return card
    }

    fun wonTrick() {
        tricksWon++
    }

    fun resetTricks() {
        tricksWon = 0
    }
}
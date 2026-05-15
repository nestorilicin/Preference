package etf.nestor.preference.domain.model

data class TrickContext(val leadSuit: Suit, val trumpSuit: Suit)
fun findStrongestCard(cards : List<PlayingCard>, context: TrickContext) : PlayingCard {
    return cards.maxBy { it.strength(context) }
}
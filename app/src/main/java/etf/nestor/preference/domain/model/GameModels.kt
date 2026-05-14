package etf.nestor.preference.domain.model

enum class Suit { SPADES, DIAMONDS, HEARTS, CLUBS, NO_SUIT } // NO_SUIT is special case for Misere (Betli)
enum class Rank(val power: Int) {
    SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13), ACE(14)
}

data class PlayingCard(val suit: Suit, val rank: Rank)
data class TrickContext(val leadSuit: Suit, val trumpSuit: Suit)
fun PlayingCard.strength(context: TrickContext) : Int {
    val isTrump = suit == context.trumpSuit
    val isLead = suit == context.leadSuit
    return when {
        isTrump -> rank.power + 200
        isLead -> rank.power + 100
        else -> rank.power
    }
}
fun findStrongestCard(cards : List<PlayingCard>, context: TrickContext) : PlayingCard {
    return cards.maxBy { it.strength(context) }
}

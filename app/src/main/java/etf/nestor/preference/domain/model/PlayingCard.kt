package etf.nestor.preference.domain.model

data class PlayingCard(val suit: Suit, val rank: Rank)
fun PlayingCard.strength(context: TrickContext) : Int {
    val isTrump = suit == context.trumpSuit
    val isLead = suit == context.leadSuit
    return when {
        isTrump -> rank.power + 200
        isLead -> rank.power + 100
        else -> rank.power
    }
}

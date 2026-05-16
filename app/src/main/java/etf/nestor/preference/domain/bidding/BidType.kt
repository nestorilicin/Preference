package etf.nestor.preference.domain.bidding

enum class BidType(val strength : Int) {
    SPADES(2),
    DIAMONDS(3),
    HEARTS(4),
    CLUBS(5),
    MISERE(6),
    SANS(7);
    companion object {
        fun fromValue(value : Int) : BidType? {
            return entries.find { it.strength == value }
        }
    }
}
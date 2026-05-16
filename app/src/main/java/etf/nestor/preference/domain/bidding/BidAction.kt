package etf.nestor.preference.domain.bidding

enum class BidAction() {
    PASS,
    MINE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN;
    fun isNumberBid() : Boolean {
        return this.ordinal > 1
    }
    fun isCheck() : Boolean {
        return this == PASS
    }
    fun isMine() : Boolean {
        return this == MINE
    }
}
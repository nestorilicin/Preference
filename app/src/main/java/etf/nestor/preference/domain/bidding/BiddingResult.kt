package etf.nestor.preference.domain.bidding

data class BiddingResult (
    val winner : Int?,
    val winningValue : Int?,
    val everyonePassed : Boolean
)
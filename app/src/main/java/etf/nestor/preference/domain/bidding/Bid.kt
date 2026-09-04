package etf.nestor.preference.domain.bidding

import kotlinx.serialization.Serializable

@Serializable
data class Bid(val playerPosition : Int, val action : BidAction)

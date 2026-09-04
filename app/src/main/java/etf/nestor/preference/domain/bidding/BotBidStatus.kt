package etf.nestor.preference.domain.bidding

sealed class BotBidStatus {
    object Thinking : BotBidStatus()
    object Pass : BotBidStatus()

    object Good : BotBidStatus()
}

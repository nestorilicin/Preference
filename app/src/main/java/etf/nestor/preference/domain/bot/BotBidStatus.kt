package etf.nestor.preference.domain.bot

sealed class BotBidStatus {
    object Thinking : BotBidStatus()
    object Pass : BotBidStatus()

    object Good : BotBidStatus()
}
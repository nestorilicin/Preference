package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.BidType

sealed class GameMessage {

    object Loading : GameMessage()
    object YourTurnBid : GameMessage()
    object TookTalon : GameMessage()
    object DeclareGamePrompt : GameMessage()
    object AllPassed : GameMessage()
    object TrickFinished : GameMessage()
    object Continuing : GameMessage()
    object YourTurn : GameMessage()
    object YouPlayedCard : GameMessage()
    object NobodyFollows : GameMessage()
    object GameStarted : GameMessage()
    object AskFollow : GameMessage()
    object GameOver : GameMessage()

    data class NewRound(val dealerName: String, val firstBidderName: String) : GameMessage()
    data class YouWonBid(val value: Int) : GameMessage()
    data class BotWonBid(val botPos: Int) : GameMessage()

    data class BotTakesTalon(val name: String) : GameMessage()
    data class BotPlaysMisere(val name: String) : GameMessage()
    data class BotPlaysGame(val name: String, val gameType: BidType) : GameMessage()
    data class FollowersJoin(val names: String) : GameMessage()
    data class BotPlayedCard(val name: String) : GameMessage()
    data class RoundFinished(val name: String, val won: Boolean) : GameMessage()
    data class IllegalMove(val legalCount: Int) : GameMessage()

    data class BotAskedToBid(val name: String) : GameMessage()
    data class BotPasses(val name: String) : GameMessage()
    data class BotSaysGood(val name: String) : GameMessage()

    data class BotAskedToFollow(val name: String) : GameMessage()
    data class BotFollows(val name: String) : GameMessage()
}

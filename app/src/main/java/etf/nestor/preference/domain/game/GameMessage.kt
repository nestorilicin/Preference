package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.BidType

/**
 * Tipizovane poruke koje fasada emituje.
 * UI sloj ih mapira na lokalizovane stringove putem stringResource().
 * Domen ostaje čist — nema Android zavisnosti.
 */
sealed class GameMessage {
    // Poruke bez parametara
    object Loading           : GameMessage()
    object YourTurnBid       : GameMessage()
    object TookTalon         : GameMessage()
    object DeclareGamePrompt : GameMessage()
    object AllPassed         : GameMessage()
    object TrickFinished     : GameMessage()
    object Continuing        : GameMessage()
    object YourTurn          : GameMessage()
    object YouPlayedCard     : GameMessage()
    object NobodyFollows     : GameMessage()
    object GameStarted       : GameMessage()
    object AskFollow         : GameMessage()
    object GameOver          : GameMessage()

    // Poruke sa parametrima
    data class NewRound(val dealerName: String, val firstBidderName: String) : GameMessage()
    data class YouWonBid(val value: Int)                            : GameMessage()
    data class BotWonBid(val botPos: Int)                          : GameMessage()
    // Bot je pobedio licitaciju i uzima kup — kup se otkriva svima pre nego što
    // ga bot stavi u svoju ruku
    data class BotTakesTalon(val name: String)                     : GameMessage()
    data class BotPlaysMisere(val name: String)                    : GameMessage()
    data class BotPlaysGame(val name: String, val gameType: BidType): GameMessage()
    data class FollowersJoin(val names: String)                    : GameMessage()
    data class BotPlayedCard(val name: String)                     : GameMessage()
    data class RoundFinished(val name: String, val won: Boolean)   : GameMessage()
    data class IllegalMove(val legalCount: Int)                    : GameMessage()

    // Licitacija — bot "govori" (vidi i BotBidStatus, koji drži trenutni status svakog bota)
    data class BotAskedToBid(val name: String)                     : GameMessage()
    data class BotPasses(val name: String)                         : GameMessage()
    data class BotSaysGood(val name: String)                       : GameMessage()

    // Praćenje — bot "govori" da li dolazi
    data class BotAskedToFollow(val name: String)                  : GameMessage()
    data class BotFollows(val name: String)                        : GameMessage()
}

package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.BidType

/**
 * Tipizovane poruke koje fasada emituje.
 * UI sloj ih mapira na lokalizovane stringove putem stringResource().
 * Domen ostaje čist — nema Android zavisnosti.
 */
sealed class GameMessage {
    // Poruke bez parametara
    object Loading          : GameMessage()
    object NewRound         : GameMessage()
    object YourTurnBid      : GameMessage()
    object TookTalon        : GameMessage()
    object DeclareGamePrompt: GameMessage()
    object AllPassed        : GameMessage()
    object TrickFinished    : GameMessage()
    object Continuing       : GameMessage()
    object YourTurn         : GameMessage()
    object YouPlayedCard    : GameMessage()
    object NobodyFollows    : GameMessage()
    object GameStarted      : GameMessage()
    object AskFollow        : GameMessage()

    // Poruke sa parametrima
    data class YouWonBid(val value: Int)                            : GameMessage()
    data class BotWonBid(val botPos: Int)                          : GameMessage()
    data class BotPlaysMisere(val name: String)                    : GameMessage()
    data class BotPlaysGame(val name: String, val gameType: BidType): GameMessage()
    data class FollowersJoin(val names: String)                    : GameMessage()
    data class BotPlayedCard(val name: String)                     : GameMessage()
    data class RoundFinished(val name: String, val won: Boolean)   : GameMessage()
    data class IllegalMove(val legalCount: Int)                    : GameMessage()
}

package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.Bid
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.scoring.ScoreSheet
import kotlinx.serialization.Serializable

@Serializable
data class SavedGameState(
    // Imena igrača po poziciji — pozicija 0 je čovek
    val playerNames: List<String>,
    val dealerPosition: Int,
    val scoreSheets: Map<Int, ScoreSheet>,
    // Karte u ruci svakog igrača, po poziciji
    val playerHands: List<List<PlayingCard>>,
    val tricksWon: List<Int>,
    val talonCards: List<PlayingCard>,
    val minimumBidValue: Int,
    val currentGameType: BidType?,
    val wonTricksByPlayer: Map<Int, List<List<PlayingCard>>>,
    // Stanje tekuće ruke
    val phase: HandPhase,
    val declarer: Int?,
    val trumpSuit: Suit?,
    val handCurrentPlayer: Int,
    // Kompletna istorija licitiranja ove ruke — dovoljna da se BiddingManager rekonstruiše replay-om
    val bids: List<Bid>,
    val finishedTricks: List<List<PlayedCard>>,
    val currentTrickCards: List<PlayedCard>,
    // Odluke praćenja (desni pa levi igrač u odnosu na deklaranta), null = još nije odlučio
    val followRightDecision: Boolean?,
    val followLeftDecision: Boolean?
)

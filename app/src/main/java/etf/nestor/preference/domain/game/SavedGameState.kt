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

    val playerNames: List<String>,
    val dealerPosition: Int,
    val scoreSheets: Map<Int, ScoreSheet>,

    val playerHands: List<List<PlayingCard>>,
    val tricksWon: List<Int>,
    val talonCards: List<PlayingCard>,
    val minimumBidValue: Int,
    val currentGameType: BidType?,
    val wonTricksByPlayer: Map<Int, List<List<PlayingCard>>>,

    val phase: HandPhase,
    val declarer: Int?,
    val trumpSuit: Suit?,
    val handCurrentPlayer: Int,

    val bids: List<Bid>,
    val finishedTricks: List<List<PlayedCard>>,
    val currentTrickCards: List<PlayedCard>,

    val followRightDecision: Boolean?,
    val followLeftDecision: Boolean?
)

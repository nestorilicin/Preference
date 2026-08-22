package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.scoring.ScoreSheet

data class GameSnapshot(
    val phase: HandPhase,
    val dealerPosition: Int,
    val playerHand: List<PlayingCard>,
    val talon: List<PlayingCard>,
    val currentTrick: List<PlayedCard>,
    val tricksWon: List<Int>,
    val biddingHighestBid: Int,
    val biddingCurrentWinner: Int?,
    val biddingCurrentPlayer: Int?,
    val declarer: Int?,
    val trumpSuit: Suit?,
    val minimumBidValue: Int,
    val followStatus: Map<Int, Boolean?>,
    val scoreSheets: Map<Int, ScoreSheet>,
    val message: GameMessage,
    val isHumanTurn: Boolean,
    val legalCardsForHuman: List<PlayingCard> = emptyList(),
    // Karte grupovane po stihovima, po igraču — za pregled preuzetih stihova
    val wonTricksByPlayer: Map<Int, List<List<PlayingCard>>> = emptyMap()
)

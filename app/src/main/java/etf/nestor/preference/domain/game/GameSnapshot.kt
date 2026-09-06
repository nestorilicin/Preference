package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.bot.BotBidStatus
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.scoring.ScoreSheet

data class GameSnapshot(
    val phase: HandPhase,
    val dealerPosition: Int,
    val playerNames: List<String>,
    val playerHand: List<PlayingCard>,
    val talon: List<PlayingCard>,
    val currentTrick: List<PlayedCard>,
    val tricksWon: List<Int>,
    val handSizes: List<Int>,
    val biddingHighestBid: Int,
    val biddingCurrentWinner: Int?,
    val biddingCurrentPlayer: Int?,
    val declarer: Int?,
    val trumpSuit: Suit?,
    val currentGameType: BidType?,
    val minimumBidValue: Int,
    val followStatus: Map<Int, Boolean?>,
    val scoreSheets: Map<Int, ScoreSheet>,
    val message: GameMessage,
    val isHumanTurn: Boolean,
    val legalCardsForHuman: List<PlayingCard> = emptyList(),
    val wonTricksByPlayer: Map<Int, List<List<PlayingCard>>> = emptyMap(),
    val biddingBotStatus: Map<Int, BotBidStatus> = emptyMap(),
    val gameOver: Boolean = false
)

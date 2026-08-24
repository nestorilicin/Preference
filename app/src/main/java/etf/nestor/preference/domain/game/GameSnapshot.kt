package etf.nestor.preference.domain.game

import etf.nestor.preference.domain.bidding.BotBidStatus
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.scoring.ScoreSheet

data class GameSnapshot(
    val phase: HandPhase,
    val dealerPosition: Int,
    // Imena igrača po poziciji — pozicija 0 je stvarno (perzistirano) ime igrača
    val playerNames: List<String>,
    val playerHand: List<PlayingCard>,
    val talon: List<PlayingCard>,
    val currentTrick: List<PlayedCard>,
    val tricksWon: List<Int>,
    // Broj karata koje svaki igrač trenutno drži u ruci (po poziciji) — koristi se za prikaz
    // broja karata botova, azurira se cim odigraju kartu (ne tek kad se zavrsi stih)
    val handSizes: List<Int>,
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
    val wonTricksByPlayer: Map<Int, List<List<PlayingCard>>> = emptyMap(),
    // Šta bot trenutno "govori" tokom licitacije (pozicija -> status) — postoji samo dok je
    // phase == HandPhase.BIDDING, koristi se za tekst pored bota u UI-u
    val biddingBotStatus: Map<Int, BotBidStatus> = emptyMap(),
    // Partija je gotova (zbir svih tabela je pao na 0 ili niže) — UI prikazuje konačan
    // rezultat umesto normalne table za igru
    val gameOver: Boolean = false
)

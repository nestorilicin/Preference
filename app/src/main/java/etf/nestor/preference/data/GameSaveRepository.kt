package etf.nestor.preference.data

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import etf.nestor.preference.domain.bidding.Bid
import etf.nestor.preference.domain.bidding.BidAction
import etf.nestor.preference.domain.bidding.BidType
import etf.nestor.preference.domain.game.HandPhase
import etf.nestor.preference.domain.game.SavedGameState
import etf.nestor.preference.domain.model.PlayedCard
import etf.nestor.preference.domain.model.PlayingCard
import etf.nestor.preference.domain.model.Rank
import etf.nestor.preference.domain.model.Suit
import etf.nestor.preference.domain.scoring.ScoreSheet
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Čuva/učitava sačuvanu singleplayer partiju (za Continue) kao JSON u SharedPreferences.
 * Ručna (de)serijalizacija — model je mali, nema potrebe za dodatnom bibliotekom.
 */
@Singleton
class GameSaveRepository @Inject constructor(
    application: Application
) {
    private val prefs = application.getSharedPreferences("singleplayer_save", Context.MODE_PRIVATE)

    fun hasSavedGame(): Boolean = prefs.contains(KEY_SAVE)

    fun save(state: SavedGameState) {
        prefs.edit { putString(KEY_SAVE, state.toJson().toString()) }
    }

    fun load(): SavedGameState? {
        val raw = prefs.getString(KEY_SAVE, null) ?: return null
        return try {
            JSONObject(raw).toSavedGameState()
        } catch (_: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit { remove(KEY_SAVE) }
    }

    companion object {
        private const val KEY_SAVE = "saved_game"
    }
}

// ─── (De)serijalizacija — SavedGameState <-> JSON ────────────────────────
// Napomena: funkcije za List<PlayingCard> i List<PlayedCard> moraju imati različita imena —
// posle brisanja generičkih tipova (JVM erasure) obe bi inače imale isti JVM potpis
// (List.toJsonArray()) i to je "platform declaration clash" greška pri kompajliranju.

private fun PlayingCard.toJsonValue(): String = "${suit.name}:${rank.name}"

private fun String.toPlayingCard(): PlayingCard {
    val (suit, rank) = split(":")
    return PlayingCard(Suit.valueOf(suit), Rank.valueOf(rank))
}

private fun List<PlayingCard>.toCardsJsonArray(): JSONArray =
    JSONArray().apply { forEach { put(it.toJsonValue()) } }

private fun JSONArray.toPlayingCards(): List<PlayingCard> =
    (0 until length()).map { getString(it).toPlayingCard() }

private fun PlayedCard.toJsonObject(): JSONObject =
    JSONObject().apply {
        put("pos", playerPosition)
        put("card", card.toJsonValue())
    }

private fun JSONObject.toPlayedCard(): PlayedCard =
    PlayedCard(getInt("pos"), getString("card").toPlayingCard())

private fun List<PlayedCard>.toPlayedCardsJsonArray(): JSONArray =
    JSONArray().apply { forEach { put(it.toJsonObject()) } }

private fun JSONArray.toPlayedCards(): List<PlayedCard> =
    (0 until length()).map { getJSONObject(it).toPlayedCard() }

private fun ScoreSheet.toJsonObject(): JSONObject =
    JSONObject().apply {
        put("center", center)
        put("soups", JSONObject().apply { soups.forEach { (pos, value) -> put(pos.toString(), value) } })
    }

private fun JSONObject.toScoreSheet(): ScoreSheet {
    val soupsJson = getJSONObject("soups")
    val soups = mutableMapOf<Int, Int>()
    soupsJson.keys().forEach { key -> soups[key.toInt()] = soupsJson.getInt(key) }
    return ScoreSheet(getInt("center"), soups)
}

private fun SavedGameState.toJson(): JSONObject = JSONObject().apply {
    put("playerNames", JSONArray(playerNames))
    put("dealerPosition", dealerPosition)
    put("scoreSheets", JSONObject().apply {
        scoreSheets.forEach { (pos, sheet) -> put(pos.toString(), sheet.toJsonObject()) }
    })
    put("playerHands", JSONArray().apply { playerHands.forEach { put(it.toCardsJsonArray()) } })
    put("tricksWon", JSONArray(tricksWon))
    put("talonCards", talonCards.toCardsJsonArray())
    put("minimumBidValue", minimumBidValue)
    currentGameType?.let { put("currentGameType", it.name) }
    put("wonTricksByPlayer", JSONObject().apply {
        wonTricksByPlayer.forEach { (pos, tricks) ->
            put(pos.toString(), JSONArray().apply { tricks.forEach { put(it.toCardsJsonArray()) } })
        }
    })
    put("phase", phase.name)
    declarer?.let { put("declarer", it) }
    trumpSuit?.let { put("trumpSuit", it.name) }
    put("handCurrentPlayer", handCurrentPlayer)
    put("bids", JSONArray().apply {
        bids.forEach { put(JSONObject().apply { put("pos", it.playerPosition); put("action", it.action.name) }) }
    })
    put("finishedTricks", JSONArray().apply { finishedTricks.forEach { put(it.toPlayedCardsJsonArray()) } })
    put("currentTrickCards", currentTrickCards.toPlayedCardsJsonArray())
    followRightDecision?.let { put("followRightDecision", it) }
    followLeftDecision?.let { put("followLeftDecision", it) }
}

private fun JSONObject.toSavedGameState(): SavedGameState {
    val playerNamesJson = getJSONArray("playerNames")
    val playerNames = (0 until playerNamesJson.length()).map { playerNamesJson.getString(it) }

    val scoreSheetsJson = getJSONObject("scoreSheets")
    val scoreSheets = mutableMapOf<Int, ScoreSheet>()
    scoreSheetsJson.keys().forEach { key -> scoreSheets[key.toInt()] = scoreSheetsJson.getJSONObject(key).toScoreSheet() }

    val playerHandsJson = getJSONArray("playerHands")
    val playerHands = (0 until playerHandsJson.length()).map { playerHandsJson.getJSONArray(it).toPlayingCards() }

    val tricksWonJson = getJSONArray("tricksWon")
    val tricksWon = (0 until tricksWonJson.length()).map { tricksWonJson.getInt(it) }

    val wonTricksJson = getJSONObject("wonTricksByPlayer")
    val wonTricksByPlayer = mutableMapOf<Int, List<List<PlayingCard>>>()
    wonTricksJson.keys().forEach { key ->
        val tricksArray = wonTricksJson.getJSONArray(key)
        wonTricksByPlayer[key.toInt()] = (0 until tricksArray.length()).map { tricksArray.getJSONArray(it).toPlayingCards() }
    }

    val bidsJson = getJSONArray("bids")
    val bids = (0 until bidsJson.length()).map {
        val o = bidsJson.getJSONObject(it)
        Bid(o.getInt("pos"), BidAction.valueOf(o.getString("action")))
    }

    val finishedTricksJson = getJSONArray("finishedTricks")
    val finishedTricks = (0 until finishedTricksJson.length()).map { finishedTricksJson.getJSONArray(it).toPlayedCards() }

    return SavedGameState(
        playerNames = playerNames,
        dealerPosition = getInt("dealerPosition"),
        scoreSheets = scoreSheets,
        playerHands = playerHands,
        tricksWon = tricksWon,
        talonCards = getJSONArray("talonCards").toPlayingCards(),
        minimumBidValue = getInt("minimumBidValue"),
        currentGameType = if (has("currentGameType")) BidType.valueOf(getString("currentGameType")) else null,
        wonTricksByPlayer = wonTricksByPlayer,
        phase = HandPhase.valueOf(getString("phase")),
        declarer = if (has("declarer")) getInt("declarer") else null,
        trumpSuit = if (has("trumpSuit")) Suit.valueOf(getString("trumpSuit")) else null,
        handCurrentPlayer = getInt("handCurrentPlayer"),
        bids = bids,
        finishedTricks = finishedTricks,
        currentTrickCards = getJSONArray("currentTrickCards").toPlayedCards(),
        followRightDecision = if (has("followRightDecision")) getBoolean("followRightDecision") else null,
        followLeftDecision = if (has("followLeftDecision")) getBoolean("followLeftDecision") else null
    )
}

package etf.nestor.preference.data

import etf.nestor.preference.data.db.SavedGameDao
import etf.nestor.preference.data.db.SavedGameEntity
import etf.nestor.preference.data.network.PartijaApi
import etf.nestor.preference.data.network.PartijaSummary
import etf.nestor.preference.data.network.PartijaUpload
import etf.nestor.preference.domain.game.SavedGameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class SavedGameSummary(val id: Long, val name: String, val savedAt: Long)

@Serializable
data class SavedGameExport(val name: String?, val savedAt: Long, val json: String)


enum class AddGameResult { SUCCESS, DUPLICATE, FAILED, SERVER_UNAVAILABLE }

enum class UploadResult { SUCCESS, DUPLICATE, FAILED, SERVER_UNAVAILABLE }

@Singleton
class GameSaveRepository @Inject constructor(
    private val dao: SavedGameDao,
    private val partijaApi: PartijaApi,
    private val playerPreferences: PlayerPreferences
) {

    fun observeHasAutoBackup(): Flow<Boolean> = dao.observeHasAutoBackup()

    suspend fun saveAutoBackup(state: SavedGameState) = upsert(name = null, state = state)

    suspend fun loadAutoBackup(): SavedGameState? =
        dao.getAutoBackup()?.json?.toSavedGameStateOrNull()

    suspend fun clearAutoBackup() = dao.deleteAutoBackup()

    fun observeSavedGames(): Flow<List<SavedGameSummary>> =
        dao.observeSavedGames().map { entities ->
            entities.map { SavedGameSummary(it.id, it.name.orEmpty(), it.savedAt) }
        }

    suspend fun saveNamed(name: String, state: SavedGameState) = upsert(name = name, state = state)

    suspend fun loadNamed(id: Long): SavedGameState? =
        dao.getById(id)?.json?.toSavedGameStateOrNull()

    suspend fun saveOverExisting(id: Long, state: SavedGameState) {
        val existing = dao.getById(id) ?: return
        dao.update(
            existing.copy(
                savedAt = System.currentTimeMillis(),
                json = Json.encodeToString(state)
            )
        )
        dao.deleteAutoBackup()
    }

    suspend fun deleteSavedGame(id: Long) = dao.deleteById(id)

    suspend fun exportToJson(id: Long): String? {
        val entity = dao.getById(id) ?: return null
        return Json.encodeToString(SavedGameExport(entity.name, entity.savedAt, entity.json))
    }

    suspend fun importFromJson(jsonText: String): AddGameResult {
        val export = try {
            Json.decodeFromString<SavedGameExport>(jsonText)
        } catch (_: Exception) {
            return AddGameResult.FAILED
        }
        val state = export.json.toSavedGameStateOrNull() ?: return AddGameResult.FAILED
        val name = export.name?.ifBlank { null } ?: "Uvezena partija"
        if (dao.existsByNameAndSavedAt(name, export.savedAt)) return AddGameResult.DUPLICATE
        dao.insert(SavedGameEntity(name = name, savedAt = export.savedAt, json = Json.encodeToString(state.withLocalPlayerName())))
        return AddGameResult.SUCCESS
    }

    suspend fun uploadToServer(id: Long, password: String): UploadResult {
        val entity = dao.getById(id) ?: return UploadResult.FAILED
        return try {
            partijaApi.upload(
                PartijaUpload(
                    password = password,
                    name = entity.name,
                    savedAt = entity.savedAt,
                    json = entity.json
                )
            )
            UploadResult.SUCCESS
        } catch (_: IOException) {
            UploadResult.SERVER_UNAVAILABLE
        } catch (e: HttpException) {
            if (e.code() == 409) UploadResult.DUPLICATE else UploadResult.FAILED
        } catch (_: Exception) {
            UploadResult.FAILED
        }
    }

    suspend fun listServerPartije(): List<PartijaSummary>? =
        try {
            partijaApi.listPartije()
        } catch (_: Exception) {
            null
        }

    suspend fun downloadFromServer(id: Int, password: String): AddGameResult {
        val partija = try {
            partijaApi.download(id, password)
        } catch (_: IOException) {
            return AddGameResult.SERVER_UNAVAILABLE
        } catch (_: Exception) {
            return AddGameResult.FAILED
        }
        val state = partija.json.toSavedGameStateOrNull() ?: return AddGameResult.FAILED
        val name = partija.name?.ifBlank { null } ?: "Preuzeta partija"
        if (dao.existsByNameAndSavedAt(name, partija.savedAt)) return AddGameResult.DUPLICATE
        dao.insert(SavedGameEntity(name = name, savedAt = partija.savedAt, json = Json.encodeToString(state.withLocalPlayerName())))
        return AddGameResult.SUCCESS
    }

    private suspend fun upsert(name: String?, state: SavedGameState) {
        val existing = dao.getAutoBackup()
        val entity = SavedGameEntity(
            id = existing?.id ?: 0,
            name = name,
            savedAt = System.currentTimeMillis(),
            json = Json.encodeToString(state)
        )
        if (existing != null) dao.update(entity) else dao.insert(entity)
    }

    /** Zamenjuje ime igrača na poziciji 0 (čovek) imenom iz podešavanja ovog uređaja, umesto imena iz partije. */
    private fun SavedGameState.withLocalPlayerName(): SavedGameState {
        if (playerNames.isEmpty()) return this
        val localName = playerPreferences.getName().ifBlank { "Igrač" }
        return copy(playerNames = playerNames.toMutableList().also { it[0] = localName })
    }
}

private fun String.toSavedGameStateOrNull(): SavedGameState? =
    try {
        Json.decodeFromString<SavedGameState>(this)
    } catch (_: Exception) {
        null
    }

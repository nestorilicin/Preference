package etf.nestor.preference.data

import etf.nestor.preference.data.db.SavedGameDao
import etf.nestor.preference.data.db.SavedGameEntity
import etf.nestor.preference.domain.game.SavedGameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class SavedGameSummary(val id: Long, val name: String, val savedAt: Long)

@Singleton
class GameSaveRepository @Inject constructor(
    private val dao: SavedGameDao
) {

    suspend fun hasAutoBackup(): Boolean = dao.getAutoBackup() != null

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

    suspend fun deleteSavedGame(id: Long) = dao.deleteById(id)

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
}

private fun String.toSavedGameStateOrNull(): SavedGameState? =
    try {
        Json.decodeFromString<SavedGameState>(this)
    } catch (_: Exception) {
        null
    }

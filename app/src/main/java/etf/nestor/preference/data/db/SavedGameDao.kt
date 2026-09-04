package etf.nestor.preference.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedGameDao {

    @Query("SELECT * FROM saved_games WHERE name IS NOT NULL ORDER BY savedAt DESC")
    fun observeSavedGames(): Flow<List<SavedGameEntity>>

    @Query("SELECT * FROM saved_games WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedGameEntity?

    @Query("SELECT * FROM saved_games WHERE name IS NULL LIMIT 1")
    suspend fun getAutoBackup(): SavedGameEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_games WHERE name IS NULL)")
    fun observeHasAutoBackup(): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_games WHERE name = :name AND savedAt = :savedAt)")
    suspend fun existsByNameAndSavedAt(name: String, savedAt: Long): Boolean

    @Insert
    suspend fun insert(entity: SavedGameEntity): Long

    @Update
    suspend fun update(entity: SavedGameEntity)

    @Query("DELETE FROM saved_games WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_games WHERE name IS NULL")
    suspend fun deleteAutoBackup()
}

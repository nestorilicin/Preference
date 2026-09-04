package etf.nestor.preference.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class SavedGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String?,
    val savedAt: Long,
    val json: String
)

package etf.nestor.preference.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SavedGameEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedGameDao(): SavedGameDao
}

package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val id: Int, // Level number, 1 to 20
    val isUnlocked: Boolean,
    val stars: Int, // 0 to 3
    val highScore: Int = 0
)

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY id ASC")
    fun getAllProgressFlow(): Flow<List<LevelProgress>>

    @Query("SELECT * FROM level_progress ORDER BY id ASC")
    suspend fun getAllProgress(): List<LevelProgress>

    @Query("SELECT * FROM level_progress WHERE id = :levelId")
    suspend fun getProgressForLevel(levelId: Int): LevelProgress?

    @Query("UPDATE level_progress SET isUnlocked = 1 WHERE id = :levelId")
    suspend fun unlockLevel(levelId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: LevelProgress)
}

@Database(entities = [LevelProgress::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract val dao: LevelProgressDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "simsim_games_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class GameRepository(private val dao: LevelProgressDao) {
    val allProgressFlow: Flow<List<LevelProgress>> = dao.getAllProgressFlow()

    suspend fun checkAndInitializeLevels() {
        val existing = dao.getAllProgress()
        if (existing.isEmpty()) {
            val initialList = List(20) { index ->
                LevelProgress(
                    id = index + 1,
                    isUnlocked = index == 0, // Level 1 is unlocked initially
                    stars = 0,
                    highScore = 0
                )
            }
            for (level in initialList) {
                dao.saveProgress(level)
            }
        }
    }

    suspend fun saveProgress(levelId: Int, stars: Int, score: Int) {
        val current = dao.getProgressForLevel(levelId)
        val maxStars = maxOf(current?.stars ?: 0, stars)
        val maxScore = maxOf(current?.highScore ?: 0, score)
        
        dao.saveProgress(
            LevelProgress(
                id = levelId,
                isUnlocked = true,
                stars = maxStars,
                highScore = maxScore
            )
        )
        
        // Unlock next level
        if (levelId < 20) {
            val nextLevelId = levelId + 1
            val nextCurrent = dao.getProgressForLevel(nextLevelId)
            dao.saveProgress(
                LevelProgress(
                    id = nextLevelId,
                    isUnlocked = true, // Unlocked
                    stars = nextCurrent?.stars ?: 0,
                    highScore = nextCurrent?.highScore ?: 0
                )
            )
        }
    }

    suspend fun resetAllProgress() {
        val initialList = List(20) { index ->
            LevelProgress(
                id = index + 1,
                isUnlocked = index == 0,
                stars = 0,
                highScore = 0
            )
        }
        for (level in initialList) {
            dao.saveProgress(level)
        }
    }
}

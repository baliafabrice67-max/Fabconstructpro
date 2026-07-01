package com.fabconstruct.pro

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseView
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// Modèle de données pour un projet de construction
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val clientName: String,
    val dateCreated: Long = System.currentTimeMillis(),
    val status: String = "En cours",
    // Caractéristiques techniques globales
    val perimeterMeter: Double = 0.0,
    val wallHeightMeter: Double = 0.0,
    val totalBlocksNeeded: Int = 0,
    val cementBagsNeeded: Int = 0,
    val projectDurationDays: Int = 0
)

// Interface d'accès aux données (DAO)
@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY dateCreated DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Int)
}

// Instance principale de la base de données Room
@Database(entities = [Project::class], version = 1, exportSchema = false)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: ProjectDatabase? = null

        fun getDatabase(context: Context): ProjectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProjectDatabase::class.java,
                    "fabconstruct_pro_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


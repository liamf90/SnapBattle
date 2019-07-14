package com.liamfarrell.android.snapbattle.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.liamfarrell.android.snapbattle.data.AllBattlesDynamoCount
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.workers.AllBattlesFeedDatabaseWorker

/**
 * Database schema that holds the list of repos.
 */
@Database(
        entities = [ Battle::class, AllBattlesDynamoCount::class],
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AllBattlesDatabase : RoomDatabase() {

    abstract fun battlesDao(): BattleDao
    abstract fun allBattlesDynamoDataDao() : AllBattlesDynamoDataDao

    companion object {

        @Volatile
        private var INSTANCE: AllBattlesDatabase? = null

        fun getInstance(context: Context): AllBattlesDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AllBattlesDatabase::class.java, "AllBattlesFeed.db")
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                val request = OneTimeWorkRequestBuilder<AllBattlesFeedDatabaseWorker>().build()
                                WorkManager.getInstance().enqueue(request)
                            }
                        })
                 .fallbackToDestructiveMigration()
                 .build()
    }
}
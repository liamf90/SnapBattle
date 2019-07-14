package com.liamfarrell.android.snapbattle.db.following_battle_feed

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesDynamoCount
import com.liamfarrell.android.snapbattle.db.BattleDao
import com.liamfarrell.android.snapbattle.db.Converters
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.workers.FollowingBattlesFeedDatabaseWorker

/**
 * Database schema that holds the list of repos.
 */
@Database(
        entities = [ Battle::class, FollowingBattlesDynamoCount::class],
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FollowingBattlesFeedDatabase : RoomDatabase() {

    abstract fun battlesDao(): BattleDao
    abstract fun followingBattlesFeedDynamoDataDao() : FollowingBattlesFeedDynamoDataDao

    companion object {

        @Volatile
        private var INSTANCE: FollowingBattlesFeedDatabase? = null

        fun getInstance(context: Context): FollowingBattlesFeedDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        FollowingBattlesFeedDatabase::class.java, "FollowingBattlesFeed.db")
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                val request = OneTimeWorkRequestBuilder<FollowingBattlesFeedDatabaseWorker>().build()
                                WorkManager.getInstance().enqueue(request)
                            }
                        })
                 .fallbackToDestructiveMigration()
                 .build()
    }
}
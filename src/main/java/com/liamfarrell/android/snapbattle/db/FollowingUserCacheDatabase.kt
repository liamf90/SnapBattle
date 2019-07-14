package com.liamfarrell.android.snapbattle.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.liamfarrell.android.snapbattle.data.AllBattlesDynamoCount
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.workers.AllBattlesFeedDatabaseWorker
import com.liamfarrell.android.snapbattle.workers.FollowingBattlesFeedDatabaseWorker
import com.liamfarrell.android.snapbattle.workers.FollowingUserCacheDatabaseWorker


/**
 * Database schema that holds the list of users that the current user follows.
 */
@Database(
        entities = [User::class, FollowingUserDynamoCount::class],
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FollowingUserCacheDatabase : RoomDatabase() {

    abstract fun followingUserDao(): FollowingUserDao
    abstract fun followingUserDynamoCountDao() : FollowingUserDynamoDataDao

    companion object {

        @Volatile
        private var INSTANCE: FollowingUserCacheDatabase? = null

        fun getInstance(context: Context): FollowingUserCacheDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        FollowingUserCacheDatabase::class.java, "FollowingUserCacheDatabase.db")
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                val request = OneTimeWorkRequestBuilder<FollowingUserCacheDatabaseWorker>()
                                        .setConstraints(Constraints.Builder()
                                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                                .build())
                                        .build()
                                WorkManager.getInstance().enqueue(request)
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build()
    }
}
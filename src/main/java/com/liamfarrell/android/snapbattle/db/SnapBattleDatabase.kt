package com.liamfarrell.android.snapbattle.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.liamfarrell.android.snapbattle.data.AllBattlesDynamoCount
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.notifications.NotificationDb

/**
 * Database schema for the Room Database of the app
 */
@Database(
        entities = [ Battle::class, AllBattlesDynamoCount::class, OtherUsersProfilePicUrlCache::class,
            ThumbnailSignedUrlCache::class,
            NotificationDb::class, NotificationsDynamoInfo::class,
            User::class, FollowingUserDynamoCount::class, FollowingBattlesDynamoCount::class, FollowingBattleDb::class
        ],
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SnapBattleDatabase : RoomDatabase() {
    abstract fun otherUsersProfilePicCacheDao() : OtherUsersProfilePicUrlDao

    abstract fun thumbnailSignedUrlDao() : ThumbnailSignedUrlDao

    abstract fun battlesDao(): BattleDao
    abstract fun allBattlesDynamoDataDao() : AllBattlesDynamoDataDao

    abstract fun notificationsDao(): NotificationDao
    abstract fun notificationsDynamoCountDao(): NotificationDynamoInfoDao

    abstract fun followingUserDao(): FollowingUserDao
    abstract fun followingUserDynamoCountDao() : FollowingUserDynamoDataDao

    abstract fun followingBattlesFeedDynamoDataDao() : FollowingBattlesFeedDynamoDataDao
    abstract fun followingBattlesDao() : FollowingBattleDao

//    companion object {
//
//        @Volatile
//        private var INSTANCE: SnapBattleDatabase? = null
//
//        fun getInstance(context: Context): SnapBattleDatabase =
//                INSTANCE ?: synchronized(this) {
//                    INSTANCE
//                            ?: buildDatabase(context).also { INSTANCE = it }
//                }
//
//        private fun buildDatabase(context: Context) =
//                Room.databaseBuilder(context.applicationContext,
//                        SnapBattleDatabase::class.java, "SnapBattle.db")
////                        .addCallback(object : RoomDatabase.Callback() {
////                            override fun onCreate(db: SupportSQLiteDatabase) {
////                                super.onCreate(db)
////                                //val request = OneTimeWorkRequestBuilder<AllBattlesFeedDatabaseWorker>().build()
////                                WorkManager.getInstance().enqueue(request)
////                            }
////                        })
//                 .fallbackToDestructiveMigration()
//                 .build()
//    }
}
//package com.liamfarrell.android.snapbattle.workers
//
//import android.content.Context
//import android.util.Log
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.liamfarrell.android.snapbattle.data.UserFollowingDynamodbRepository
//import com.liamfarrell.android.snapbattle.data.UserFollowingRepository
//import com.liamfarrell.android.snapbattle.db.FollowingBattlesDynamoCount
//import com.liamfarrell.android.snapbattle.db.FollowingUserDynamoCount
//import com.liamfarrell.android.snapbattle.db.SnapBattleDatabase
//import kotlinx.coroutines.coroutineScope
//import javax.inject.Inject
//
//class FollowingUserCacheDatabaseWorker(
//        private val userFollowingRepository : UserFollowingRepository,
//        private val followingDynamodbRepository: UserFollowingDynamodbRepository,
//        context: Context,
//        workerParams: WorkerParameters
//) : CoroutineWorker(context, workerParams) {
//
//    private val TAG by lazy { FollowingUserCacheDatabaseWorker::class.java.simpleName }
//
//    override suspend fun doWork(): Result = coroutineScope {
//        try {
//            val database = SnapBattleDatabase.getInstance(applicationContext)
//            val usersFollowing = userFollowingRepository.getFollowing()
//            val usersFollowingDynamoDbCount = followingDynamodbRepository.getFollowingUpdateCountDynamo()
//            if (usersFollowing.error != null){
//                 throw usersFollowing.error
//            } else{
//                database.followingUserDao().insertAll(usersFollowing.result.sqlResult)
//                database.followingUserDynamoCountDao().insert(FollowingUserDynamoCount(usersFollowingDynamoDbCount))
//            }
//
//            //OTHER
//            database.followingBattlesFeedDynamoDataDao().insert(FollowingBattlesDynamoCount())
//
//
//                    Result.success()
//        }
//        catch (ex: Exception) {
//            Log.e(TAG, "Error seeding database", ex)
//            Result.failure()
//        }
//    }
//
//
//    class Factory @Inject constructor(
//            private val userFollowingRepository : UserFollowingRepository,
//            private val followingDynamodbRepository: UserFollowingDynamodbRepository
//
//            ): ChildWorkerFactory {
//
//        override fun create(appContext: Context, params: WorkerParameters): CoroutineWorker {
//            return FollowingUserCacheDatabaseWorker(userFollowingRepository, followingDynamodbRepository, appContext, params)
//        }
//    }
//}
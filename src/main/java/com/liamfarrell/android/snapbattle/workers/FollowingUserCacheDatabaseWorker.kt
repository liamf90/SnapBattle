package com.liamfarrell.android.snapbattle.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.liamfarrell.android.snapbattle.data.AllBattlesDynamoCount
import com.liamfarrell.android.snapbattle.data.UserFollowingRepository
import com.liamfarrell.android.snapbattle.db.AllBattlesDatabase
import com.liamfarrell.android.snapbattle.db.FollowingUserCacheDatabase
import com.liamfarrell.android.snapbattle.db.FollowingUserDynamoCount
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class FollowingUserCacheDatabaseWorker(
        val userFollowingRepository : UserFollowingRepository,
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG by lazy { FollowingUserCacheDatabaseWorker::class.java.simpleName }

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val database = FollowingUserCacheDatabase.getInstance(applicationContext)
            val usersFollowing = userFollowingRepository.getFollowing()
            if (usersFollowing.error != null){
                 throw usersFollowing.error
            } else{
                database.followingUserDao().insertAll(usersFollowing.result.sqlResult)
                database.followingUserDynamoCountDao().insert(FollowingUserDynamoCount())
            }


            Result.success()
        }
        catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }


    class Factory @Inject constructor(
            private val userFollowingRepository : UserFollowingRepository
            ): ChildWorkerFactory {

        override fun create(appContext: Context, params: WorkerParameters): CoroutineWorker {
            return FollowingUserCacheDatabaseWorker(userFollowingRepository,  appContext, params)
        }
    }
}
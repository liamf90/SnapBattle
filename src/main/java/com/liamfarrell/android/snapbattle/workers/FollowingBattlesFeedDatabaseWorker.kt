package com.liamfarrell.android.snapbattle.workers

import com.liamfarrell.android.snapbattle.db.following_battle_feed.FollowingBattlesFeedDatabase
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesDynamoCount
import kotlinx.coroutines.coroutineScope

class FollowingBattlesFeedDatabaseWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG by lazy { FollowingBattlesFeedDatabaseWorker::class.java.simpleName }

    override suspend fun doWork(): Result = coroutineScope {

        try {
            val database = FollowingBattlesFeedDatabase.getInstance(applicationContext)
            database.followingBattlesFeedDynamoDataDao().insert(FollowingBattlesDynamoCount())
            Result.success()
        }
        catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }
}
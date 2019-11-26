//package com.liamfarrell.android.snapbattle.workers
//
//import android.content.Context
//import android.util.Log
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.liamfarrell.android.snapbattle.data.AllBattlesDynamoCount
//import com.liamfarrell.android.snapbattle.db.SnapBattleDatabase
//import kotlinx.coroutines.coroutineScope
//
//class AllBattlesFeedDatabaseWorker(
//        context: Context,
//        workerParams: WorkerParameters
//) : CoroutineWorker(context, workerParams) {
//
//    private val TAG by lazy { AllBattlesFeedDatabaseWorker::class.java.simpleName }
//
//    override suspend fun doWork(): Result = coroutineScope {
//
//        try {
//            val database = SnapBattleDatabase.getInstance(applicationContext)
//            database.allBattlesDynamoDataDao().insert(AllBattlesDynamoCount())
//            Result.success()
//            }
//         catch (ex: Exception) {
//            Log.e(TAG, "Error seeding database", ex)
//            Result.failure()
//        }
//    }
//}
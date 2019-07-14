package com.liamfarrell.android.snapbattle.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CopyObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesDynamoCount
import com.liamfarrell.android.snapbattle.db.following_battle_feed.FollowingBattlesFeedDatabase
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Video
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import kotlinx.coroutines.coroutineScope
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun uploadVideoJob(context: Context, b : Battle, fileName : String, cognitoIDOpponent : String, videoID : Int) : AsyncTaskResult<Boolean> {


    val s3 = AmazonS3Client(FacebookLoginFragment.getCredentialsProvider(context))
    val bucketName = "snapbattlevideos"
    val CognitoID = FacebookLoginFragment.getCredentialsProvider(context).identityId
    val file = File(context.getFilesDir().getAbsolutePath() + "/" + fileName)
    val key = CognitoID + "/" + file.getName()
    val CognitoIDUser = FacebookLoginFragment.getCredentialsProvider(context).identityId
    val orientationLock = Video.orientationHintToLock(Integer.parseInt(Video.getVideoRotation(context, b.videos.get(b.videosUploaded))))

    println("Uploading a new object to S3 from a file\n")
    val por = PutObjectRequest(bucketName, key, file)
    return suspendCoroutine<AsyncTaskResult<Boolean>> {
        try {
            por.setGeneralProgressListener { arg0 ->
                if (arg0.eventCode == ProgressEvent.COMPLETED_EVENT_CODE) {
                    //Video has been uploaded. copy video to opponents bucket

                    val dstKey = cognitoIDOpponent + "/" + fileName
                    val srcKey = "$CognitoIDUser/$fileName"
                    val cor = CopyObjectRequest(bucketName, srcKey, bucketName, dstKey)
                    s3.copyObject(cor)
                    it.resume(AsyncTaskResult(true))
                }
            }
            s3.putObject(por)
        } catch (ase: AmazonServiceException) {
            it.resume(AsyncTaskResult(ase))
        } catch (ace: AmazonClientException) {
            it.resume(AsyncTaskResult(ace))
        }
    }
}




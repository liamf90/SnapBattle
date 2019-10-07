package com.liamfarrell.android.snapbattle.util

import android.content.Context
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.event.ProgressEvent
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CopyObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun uploadVideoJob(context: Context, b : Battle, fileName : String, cognitoIDOpponent : String, videoID : Int) : AsyncTaskResult<Boolean> {

        val s3 = AmazonS3Client(IdentityManager.getDefaultIdentityManager().credentialsProvider)
        val bucketName = "snapbattlevideos"
        val CognitoID = IdentityManager.getDefaultIdentityManager().cachedUserID
        val file = File(context.getFilesDir().getAbsolutePath() + "/" + fileName)
        val key = CognitoID + "/" + file.getName()
        val CognitoIDUser = IdentityManager.getDefaultIdentityManager().cachedUserID
        val orientationLock = Video.orientationHintToLock(Integer.parseInt(Video.getVideoRotation(context, b.videos.get(b.videosUploaded))))
    Timber.i("Here3")
        println("Uploading a new object to S3 from a file\n")
        val por = PutObjectRequest(bucketName, key, file)
        return withContext(Dispatchers.IO) {suspendCoroutine<AsyncTaskResult<Boolean>> {
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


}




package com.liamfarrell.android.snapbattle.data

import android.annotation.SuppressLint
import android.content.Context
import com.amazonaws.event.ProgressListener
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.regions.Region
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateProfilePictureRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetProfileResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ProfilePicRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService, private val awsMobileClient: AWSMobileClient) {

    suspend fun uploadProfilePicRepository(context: Context, newPhotoPath: String, profilePicCount: Int)  : AsyncTaskResult<DefaultResponse>  {

            try {
                withContext(Dispatchers.IO) {uploadProfilePicture(context, newPhotoPath, profilePicCount)}
            } catch (e: Exception) {
                Timber.i(e)
                return AsyncTaskResult(e)
            }
            val uploadedProfilePicCount = profilePicCount + 1
            return updateProfilePicCount(uploadedProfilePicCount)
    }


    fun getProfilePictureSavePath(context: Context): String {
        return context.filesDir.absolutePath + "/" + awsMobileClient.identityId + "-ProfilePic.png"
    }

    suspend fun checkForUpdate(context: Context, profilePicCount : Int) : Boolean {
        //val profilePicCountResponse = getProfilePicCount()
        //return if (profilePicCountResponse.error != null){
            //val profilePicCount = profilePicCountResponse.result.sqlResult.get(0).profilePicCount
            if (profilePicCount != getProfilePicCountSharedPrefs(context)){
                getProfilePictureS3(context, profilePicCount)
                updateProfilePicCountSharedPrefs(context, profilePicCount)
                return true
            } else return false
        //} else false

    }


    private suspend fun updateProfilePicCount(uploadedProfilePicCount: Int): AsyncTaskResult<DefaultResponse> {
        return executeRestApiFunction(snapBattleApiService.updateProfilePictureCount  (UpdateProfilePictureRequest().apply{profilePicCountUploaded = uploadedProfilePicCount}))
    }


    private suspend fun getProfilePicCount(): AsyncTaskResult<GetProfileResponse> {
        return executeRestApiFunction(snapBattleApiService.getProfile ())
    }

    @SuppressLint("ApplySharedPref")
    private fun updateProfilePicCountSharedPrefs(context: Context, profilePicCount: Int) {
        val sharedPref = context.getSharedPreferences(awsMobileClient.identityId, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("PROFILE_PIC_COUNT", profilePicCount)
        editor.commit()
    }

    private fun getProfilePicCountSharedPrefs(context: Context): Int {
        val sharedPref = context.getSharedPreferences(awsMobileClient.identityId, Context.MODE_PRIVATE)
        return sharedPref.getInt("PROFILE_PIC_COUNT", 0)
    }

    private suspend fun uploadProfilePicture(context: Context, newPhotoPath: String, currentProfilePicCount: Int) =
            suspendCancellableCoroutine<Unit> { continuation ->
                val s3 = AmazonS3Client(awsMobileClient, Region.getRegion("us-east-1"))
                val bucketName = "snapbattlevideos"
                val newProfilePicFile = File(newPhotoPath)
                val fileName = getNextProfilePicturePathS3(currentProfilePicCount)
                val por = PutObjectRequest(bucketName, fileName, newProfilePicFile)
                por.generalProgressListener = ProgressListener { arg0 ->
                    if (arg0.eventCode == com.amazonaws.event.ProgressEvent.COMPLETED_EVENT_CODE) {
                        val profilePicDst = File(getProfilePictureSavePath(context))
                        try {
                            newProfilePicFile.copyTo(profilePicDst, true)
                            continuation.resumeWith(Result.success(Unit))
                        } catch (e: IOException) {
                            continuation.resumeWith(Result.failure(e))
                        }
                    }
                }
                try {
                    s3.putObject(por)
                } catch (exception: Exception) {
                    continuation.resumeWith(Result.failure(exception))
                }
            }

    private suspend fun getProfilePictureS3(context: Context, currentProfilePicCount : Int) =
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val file = File(getProfilePictureSavePath(context))
                    val s3Path = getProfilePicturePathS3(currentProfilePicCount)
                    val s3 = AmazonS3Client(awsMobileClient, Region.getRegion("us-east-1"))
                    val bucketName = "snapbattlevideos"
                    val gor = GetObjectRequest(bucketName, s3Path)
                    gor.generalProgressListener = ProgressListener { arg0 ->
                        if (arg0.eventCode == com.amazonaws.event.ProgressEvent.COMPLETED_EVENT_CODE) {
                            continuation.resumeWith(Result.success(Unit))
                        }
                    }
                    try {
                        s3.getObject(gor, file)
                    } catch (exception: Exception) {
                        continuation.resumeWith(Result.failure(exception))
                    }
                }
            }

    private fun getProfilePicturePathS3(currentProfilePicCount: Int): String {
        try {
            return awsMobileClient.identityId + "/" + awsMobileClient.identityId + "-" + currentProfilePicCount + "-ProfilePic.png"
        } catch (e: NotAuthorizedException) {
            return ""
        }
    }

    private fun getNextProfilePicturePathS3(currentProfilePicCount: Int): String {
        val nextProfilePicCount = currentProfilePicCount + 1
        return awsMobileClient.identityId + "/" + awsMobileClient.identityId + "-" + nextProfilePicCount + "-ProfilePic.png"
    }





}
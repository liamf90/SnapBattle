package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlCache
import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlDao
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleTypeSuggestionsSearchRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.SignedUrlsRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetSignedUrlsResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
class OtherUsersProfilePicUrlRepository @Inject constructor(private val otherUsersProfilePicUrlDao: OtherUsersProfilePicUrlDao, private val lambdaFunctionsInterface: LambdaFunctionsInterface){

    suspend fun getOrUpdateProfilePicSignedUrl(cognitoID: String, profilePicCount: Int, signedUrlNew: String) : String {
        val profilePicCountSignedUrlDb = getUserSignedUrlAndProfilePicCount(cognitoID)
        if (profilePicCountSignedUrlDb != null) {
            val profilePicCountDb = profilePicCountSignedUrlDb.profile_pic_count
            val signedUrlDb = profilePicCountSignedUrlDb.last_saved_signed_url
            return if (profilePicCount == profilePicCountDb) {
                if (isSignedUrlInPicassoCache(signedUrlDb)) {
                    signedUrlDb
                } else {
                    insertOrUpdateUserProfilePicSignedUrl(cognitoID, profilePicCount, signedUrlNew)
                    signedUrlNew
                }
            } else {
                insertOrUpdateUserProfilePicSignedUrl(cognitoID, profilePicCount, signedUrlNew)
                signedUrlNew
            }
        } else {
            insertOrUpdateUserProfilePicSignedUrl(cognitoID, profilePicCount, signedUrlNew)
            return signedUrlNew
        }
    }

    suspend fun deleteOtherUsersProfilePicCache(){
        withContext(IO){
            otherUsersProfilePicUrlDao.deleteAllProfilePicSignedUrls()
        }
    }

    suspend fun insertOtherUsersProfilePicOnlyIfProfilePicCountDifferent(cognitoID: String, profilePicCount: Int, signedUrl: String){
        val updatedSignedUrlAndProfilePicCount = OtherUsersProfilePicUrlCache(cognitoID, profilePicCount, signedUrl)
        withContext(IO){
            val signedUrlAndProfilePicCountDb = getUserSignedUrlAndProfilePicCount(cognitoID)
            if (signedUrlAndProfilePicCountDb == null || signedUrlAndProfilePicCountDb.profile_pic_count != updatedSignedUrlAndProfilePicCount.profile_pic_count || !isSignedUrlInPicassoCache(signedUrlAndProfilePicCountDb.last_saved_signed_url)){
                otherUsersProfilePicUrlDao.insertSignedUrl(updatedSignedUrlAndProfilePicCount)
            }
        }
    }


     suspend fun getUserSignedUrlAndProfilePicCount(cognitoID: String) : OtherUsersProfilePicUrlCache?{
        return withContext(IO) {
             otherUsersProfilePicUrlDao.getSignedUrlAndProfilePicForUser(cognitoID)
         }
    }


    private suspend fun insertOrUpdateUserProfilePicSignedUrl(cognitoIdUser: String, profilePicCount: Int, signedUrl: String){
        withContext(IO){
            otherUsersProfilePicUrlDao.insertSignedUrl(OtherUsersProfilePicUrlCache(cognitoIdUser, profilePicCount, signedUrl))
        }
    }



     suspend fun isSignedUrlInPicassoCache(signedUrl: String) : Boolean{
        return suspendCoroutine<Boolean> {
            Picasso.get().load(signedUrl).networkPolicy(NetworkPolicy.OFFLINE).fetch(
                    object : Callback{
                        override fun onSuccess() {
                            it.resumeWith(Result.success(true))
                        }

                        override fun onError(e: Exception?) {
                            it.resumeWith(Result.success(false))
                        }
                    })
            }
        }

    suspend fun getSignedUrlsFromServer(cognitoIdList : List<String>) : AsyncTaskResult<GetSignedUrlsResponse> {
        val request = SignedUrlsRequest()
        request.cognitoIdToGetSignedUrlList = cognitoIdList
        return executeAWSFunction {lambdaFunctionsInterface.GetProfilePicSignedUrls(request)}
    }

}
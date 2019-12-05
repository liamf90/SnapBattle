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
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import com.liamfarrell.android.snapbattle.util.isSignedUrlInPicassoCache
import com.liamfarrell.android.snapbattle.util.isSignedUrlInPicassoCacheRx
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
@OpenForTesting
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

    fun getOrUpdateProfilePicSignedUrlRx(cognitoID: String, profilePicCount: Int, signedUrlNew: String) : String {
        val profilePicCountSignedUrlDb = getUserSignedUrlAndProfilePicCountRx(cognitoID)
        val otherUsersProfilePicUrlCache  = profilePicCountSignedUrlDb.blockingGet()
        if (otherUsersProfilePicUrlCache != null) {
            val profilePicCountDb = otherUsersProfilePicUrlCache.profile_pic_count
            val signedUrlDb = otherUsersProfilePicUrlCache.last_saved_signed_url
            return if (profilePicCount == profilePicCountDb) {
                if (isSignedUrlInPicassoCacheRx(signedUrlDb).subscribeOn(AndroidSchedulers.mainThread()).blockingGet()) {
                    signedUrlDb
                } else {
                    insertOrUpdateUserProfilePicSignedUrlRx(cognitoID, profilePicCount, signedUrlNew)
                    signedUrlNew
                }
            } else {
                insertOrUpdateUserProfilePicSignedUrlRx(cognitoID, profilePicCount, signedUrlNew)
                signedUrlNew
            }
        } else {
            insertOrUpdateUserProfilePicSignedUrlRx(cognitoID, profilePicCount, signedUrlNew)
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

    fun insertOtherUsersProfilePicOnlyIfProfilePicCountDifferentRx(cognitoID: String, profilePicCount: Int, signedUrl: String){
        val updatedSignedUrlAndProfilePicCount = OtherUsersProfilePicUrlCache(cognitoID, profilePicCount, signedUrl)

        val signedUrlAndProfilePicCountDb = getUserSignedUrlAndProfilePicCountRx(cognitoID).blockingGet()
        if (signedUrlAndProfilePicCountDb == null || signedUrlAndProfilePicCountDb.profile_pic_count != updatedSignedUrlAndProfilePicCount.profile_pic_count || !isSignedUrlInPicassoCacheRx(signedUrlAndProfilePicCountDb.last_saved_signed_url).subscribeOn(AndroidSchedulers.mainThread()).blockingGet()){
            otherUsersProfilePicUrlDao.insertSignedUrlRx(updatedSignedUrlAndProfilePicCount)
        }

    }


     suspend fun getUserSignedUrlAndProfilePicCount(cognitoID: String) : OtherUsersProfilePicUrlCache?{
        return withContext(IO) {
             otherUsersProfilePicUrlDao.getSignedUrlAndProfilePicForUser(cognitoID)
         }
    }

    fun getUserSignedUrlAndProfilePicCountRx(cognitoID: String) : Maybe<OtherUsersProfilePicUrlCache> {
            return otherUsersProfilePicUrlDao.getSignedUrlAndProfilePicForUserRx(cognitoID)
    }


    private suspend fun insertOrUpdateUserProfilePicSignedUrl(cognitoIdUser: String, profilePicCount: Int, signedUrl: String){
        withContext(IO){
            otherUsersProfilePicUrlDao.insertSignedUrl(OtherUsersProfilePicUrlCache(cognitoIdUser, profilePicCount, signedUrl))
        }
    }

    fun insertOrUpdateUserProfilePicSignedUrlRx(cognitoIdUser: String, profilePicCount: Int, signedUrl: String){
        otherUsersProfilePicUrlDao.insertSignedUrlRx(OtherUsersProfilePicUrlCache(cognitoIdUser, profilePicCount, signedUrl))

    }


    suspend fun getSignedUrlsFromServer(cognitoIdList : List<String>) : AsyncTaskResult<GetSignedUrlsResponse> {
        val request = SignedUrlsRequest()
        request.cognitoIdToGetSignedUrlList = cognitoIdList
        return executeAWSFunction {lambdaFunctionsInterface.GetProfilePicSignedUrls(request)}
    }

    fun getSignedUrlsFromServerRx(cognitoIdList : List<String>) : Single<GetSignedUrlsResponse> {
        val request = SignedUrlsRequest()
        request.cognitoIdToGetSignedUrlList = cognitoIdList
        return Single.fromCallable {lambdaFunctionsInterface.GetProfilePicSignedUrls(request)}
    }

}
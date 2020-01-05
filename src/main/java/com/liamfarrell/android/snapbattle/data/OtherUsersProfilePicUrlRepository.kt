package com.liamfarrell.android.snapbattle.data

import com.google.gson.Gson
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlCache
import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlDao
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetSignedUrlsResponse
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import com.liamfarrell.android.snapbattle.util.isSignedUrlInPicassoCache
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class OtherUsersProfilePicUrlRepository @Inject constructor(private val otherUsersProfilePicUrlDao: OtherUsersProfilePicUrlDao, val snapBattleApiService: SnapBattleApiService){

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


    suspend fun getSignedUrlsFromServer(cognitoIdList : List<String>) : AsyncTaskResult<GetSignedUrlsResponse> {
        return executeRestApiFunction(snapBattleApiService.getSignedUrlsProfilePictures (Gson().toJson(cognitoIdList).toString()))
    }

}
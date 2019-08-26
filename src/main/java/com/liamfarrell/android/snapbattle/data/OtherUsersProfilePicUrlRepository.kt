package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlCache
import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlDao
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
class OtherUsersProfilePicUrlRepository @Inject constructor(private val otherUsersProfilePicUrlDao: OtherUsersProfilePicUrlDao){

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

    private suspend fun getUserSignedUrlAndProfilePicCount(cognitoID: String) : OtherUsersProfilePicUrlCache?{
        return withContext(IO) {
             otherUsersProfilePicUrlDao.getSignedUrlAndProfilePicForUser(cognitoID)
         }
    }

    private suspend fun insertOrUpdateUserProfilePicSignedUrl(cognitoIdUser: String, profilePicCount: Int, signedUrl: String){
        withContext(IO){
            otherUsersProfilePicUrlDao.insertSignedUrl(OtherUsersProfilePicUrlCache(cognitoIdUser, profilePicCount, signedUrl))
        }
    }



    private suspend fun isSignedUrlInPicassoCache(signedUrl: String) : Boolean{
        return suspendCoroutine<Boolean> {
            Picasso.get().load(signedUrl).networkPolicy(NetworkPolicy.OFFLINE).fetch()
                    object : Callback{
                        override fun onSuccess() {
                            it.resumeWith(Result.success(true))
                        }

                        override fun onError(e: Exception?) {
                            it.resumeWith(Result.success(false))
                        }
                    }
            }
        }

}
package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlCache
import com.liamfarrell.android.snapbattle.db.OtherUsersProfilePicUrlDao
import com.liamfarrell.android.snapbattle.db.ThumbnailSignedUrlCache
import com.liamfarrell.android.snapbattle.db.ThumbnailSignedUrlDao
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UrlLambdaRequest
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import com.liamfarrell.android.snapbattle.util.isSignedUrlInPicassoCache
import com.liamfarrell.android.snapbattle.util.isSignedUrlInPicassoCacheRx
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
class ThumbnailSignedUrlCacheRepository @Inject constructor(private val thumbnailSignedUrlDao: ThumbnailSignedUrlDao,
                                                            private val lambdaFunctionsInterface: LambdaFunctionsInterface)
{
    suspend fun getThumbnailSignedUrl(battle: Battle) : String? {
        val signedUrlDb = getLastSavedThumbnailSignedUrl(battle.battleID)
        if (signedUrlDb != null){
            //thumbnail in db cache
            //check if in picasso cache
            return if (isSignedUrlInPicassoCache(signedUrlDb)){
                signedUrlDb
            } else {
                //get from server and update cache
                val signedUrlResponse = getSignedUrlFromServer(battle)
                if (signedUrlResponse.error == null) {
                    insertOrUpdateThumbnailSignedUrl(signedUrlResponse.result, battle.battleID)
                    signedUrlResponse.result
                } else null
            }
        } else {
            //get from server and update cache
            val signedUrlResponse = getSignedUrlFromServer(battle)
            return if (signedUrlResponse.error == null) {
                insertOrUpdateThumbnailSignedUrl(signedUrlResponse.result, battle.battleID)
                signedUrlResponse.result
            } else null
        }
    }

     fun getThumbnailSignedUrlRx(battle: Battle) : String? {

        val signedUrlDb = getLastSavedThumbnailSignedUrlRx(battle.battleID)
        if (signedUrlDb != null){
            //thumbnail in db cache
            //check if in picasso cache
            return if (isSignedUrlInPicassoCacheRx(signedUrlDb).subscribeOn(AndroidSchedulers.mainThread()).blockingGet()){
                signedUrlDb
            } else {
                //get from server and update cache
                val signedUrlResponse = getSignedUrlFromServerRx(battle).subscribeOn(io()).onErrorReturn { null }.blockingGet()
                return if (signedUrlResponse != null) {
                    insertOrUpdateThumbnailSignedUrlRx(signedUrlResponse, battle.battleID)
                    signedUrlResponse
                } else null
            }
        } else {
            //get from server and update cache
            val signedUrlResponse = getSignedUrlFromServerRx(battle).subscribeOn(io()).onErrorReturn { null }.blockingGet()
            return if (signedUrlResponse != null) {
                insertOrUpdateThumbnailSignedUrlRx(signedUrlResponse, battle.battleID)
                signedUrlResponse
            } else null
        }
    }

    private suspend fun getSignedUrlFromServer(battle: Battle) : AsyncTaskResult<String>{
        val request = UrlLambdaRequest()
        request.url = battle.thumbnailServerUrl
        return executeAWSFunction { lambdaFunctionsInterface.getSignedUrl(request) }
    }

    private fun getSignedUrlFromServerRx(battle: Battle) : Single<String> {
        val request = UrlLambdaRequest()
        request.url = battle.thumbnailServerUrl
        return Single.fromCallable { lambdaFunctionsInterface.getSignedUrl(request) }
    }

    suspend fun deleteOtherUsersProfilePicCache(){
        withContext(IO){
            thumbnailSignedUrlDao.deleteAllProfilePicSignedUrls()
        }
    }

    private  fun getLastSavedThumbnailSignedUrlRx(battleId: Int) : String?{
            return thumbnailSignedUrlDao.getLastSavedThumbnailSignedUrlRx(battleId)
    }

    private suspend fun getLastSavedThumbnailSignedUrl(battleId: Int) : String?{
        return withContext(IO) {
            thumbnailSignedUrlDao.getLastSavedThumbnailSignedUrl(battleId)
         }
    }

    private suspend fun insertOrUpdateThumbnailSignedUrl(thumbnailSignedUrl: String, battleId: Int){
        withContext(IO){
            thumbnailSignedUrlDao.insertSignedUrl(ThumbnailSignedUrlCache(battleId, thumbnailSignedUrl))
        }
    }

    private fun insertOrUpdateThumbnailSignedUrlRx(thumbnailSignedUrl: String, battleId: Int){
            thumbnailSignedUrlDao.insertSignedUrlRx(ThumbnailSignedUrlCache(battleId, thumbnailSignedUrl))

    }




}
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.data.UsersBattlesRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel used in [UsersBattlesFragment].
 */
class UsersBattlesViewModel @Inject constructor(private val context: Application, private val usersBattlesRepository: UsersBattlesRepository
                                                ,  private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository, val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelLaunch() {

    private lateinit var cognitoId : String

    private val battlesResult = MutableLiveData<AsyncTaskResult<GetUsersBattlesResponse>>()

    val errorMessage : LiveData<String?> = Transformations.map(battlesResult) { result ->
        if (result.error != null){
            getErrorMessage(context, result.error)
        } else {
            null
        }
    }

    val battles : LiveData<List<Battle>> =  Transformations.map (battlesResult) { result ->
        result.result?.user_battles?.filter { !it.isDeleted }
    }

    val user : LiveData<User> =  Transformations.map (battlesResult) { result ->
        result.result?.user_profile
    }


    fun setCognitoId(cognitoID: String){
        cognitoId = cognitoID
        awsLambdaFunctionCall(true,
                suspend {
                    val response =  usersBattlesRepository.getUsersBattles(cognitoID)
                    if (response.error == null) {
                        //check if profile pic url in cache else use new signed url
                        val profile = response.result.user_profile
                        if (profile.profilePicCount > 0) {
                            response.result.user_profile.profilePicSignedUrl =  otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrl(profile.cognitoId,  profile.profilePicCount , profile.profilePicSignedUrl )
                        }
                        response.result.user_battles = getThumbnailSignedUrls(response.result.user_battles)
                    }
                    battlesResult.value = response

                })
    }

    fun followUser(){
        awsLambdaFunctionCall(false,
                suspend {
                    val response = usersBattlesRepository.followUser(cognitoId)
                    if (response.error == null){
                        battlesResult.value?.result?.user_profile?.isFollowing = true
                        battlesResult.notifyObserver()
                    } else{
                        battlesResult.value?.error = response.error
                        battlesResult.notifyObserver()
                    }})
    }

    fun unfollowUser(){
        awsLambdaFunctionCall(false,
                suspend {
                    val response = usersBattlesRepository.unfollowUser(cognitoId)
                    if (response.error == null){
                        battlesResult.value?.result?.user_profile?.isFollowing = false
                        battlesResult.notifyObserver()
                    } else{
                        battlesResult.value?.error = response.error
                        battlesResult.notifyObserver()
                    }
                }
        )
    }

    private suspend fun getThumbnailSignedUrls(battleList: List<Battle>) : List<Battle>{
        battleList.forEach {
            it.signedThumbnailUrl = thumbnailSignedUrlCacheRepository.getThumbnailSignedUrl(it)
        }
        return battleList
    }


}
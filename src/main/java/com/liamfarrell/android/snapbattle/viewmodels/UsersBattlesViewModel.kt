package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.data.UsersBattlesRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import javax.inject.Inject

/**
 * The ViewModel used in [UsersBattlesFragment].
 */
class UsersBattlesViewModel @Inject constructor(private val context: Application, private val usersBattlesRepository: UsersBattlesRepository
                                                ,  private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository, val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelBase() {

    private lateinit var cognitoId : String

    private val battlesResult = MutableLiveData<AsyncTaskResult<GetUsersBattlesResponse>>()

    val errorMessage : LiveData<String?> = Transformations.map(battlesResult) { result ->
        if (result.error != null){
            getErrorMessage(context, result.error)
        } else {
            null
        }
    }

    val battles = MediatorLiveData<List<Battle>>()
    val user = MediatorLiveData<User>()


    init {
        battles.addSource(battlesResult){
            if (it.error == null) {
                battles.value = it.result?.user_battles?.filter { !it.isDeleted }
            }
        }
        user.addSource(battlesResult){
            if (it.error == null) {
                user.value = it.result?.user_profile
            }
        }
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

    fun setFacebookId(facebookId: String){
        awsLambdaFunctionCall(true,
                suspend {
                    val response =  usersBattlesRepository.getUsersBattlesWithFacebookId(facebookId)
                    if (response.error == null) {
                        cognitoId = response.result.user_profile.cognitoId
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
                    user.value?.isFollowingChangeInProgress = true
                    user.value?.isFollowing = true
                    user.notifyObserver()
                    val response = usersBattlesRepository.followUser(cognitoId)
                    if (response.error != null){
                        user.value?.isFollowingChangeInProgress = false
                        user.value?.isFollowing = false
                        user.notifyObserver()
                        battlesResult.value = AsyncTaskResult(response.error)
                    } else {
                        user.value?.isFollowingChangeInProgress = false
                        user.notifyObserver()
                    }})
    }

    fun unfollowUser(){
        awsLambdaFunctionCall(false,
                suspend {
                    user.value?.isFollowingChangeInProgress = true
                    user.value?.isFollowing = false
                    user.notifyObserver()
                    val response = usersBattlesRepository.unfollowUser(cognitoId)
                    if (response.error != null){
                        user.value?.isFollowing = true
                        user.value?.isFollowingChangeInProgress = false
                        user.notifyObserver()
                        battlesResult.value = AsyncTaskResult(response.error)
                    } else {
                        user.value?.isFollowingChangeInProgress = false
                        user.notifyObserver()
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
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import javax.inject.Inject

/**
 * The ViewModel used in [ViewFollowingFragment].
 */
class FollowingViewModel @Inject constructor(private val context: Application, private val followingRepository : FollowingRepository,
                                             private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository, private val followingUserCacheManager: FollowingUserCacheManager) : ViewModelBase() {

    private val profilePicMap = mutableMapOf<String, String>()

    private val followingUsersResponse = MutableLiveData<AsyncTaskResult<ResponseFollowing>>()

    val following = MediatorLiveData<MutableList<User>>()

    val errorMessage : LiveData<String?> = Transformations.map(followingUsersResponse) { asyncResult ->
        if (asyncResult.error != null) {
            getErrorMessage(context, asyncResult.error)
        } else {
            null
        } }


    init {
        following.addSource(followingUsersResponse){
            if (it.error == null) {
               following.value = it.result.sqlResult
            }
        }

        awsLambdaFunctionCall(true,
                suspend {
                    val response = followingRepository.getFollowing()
                    if (response.error == null) {
                        //set all users as following
                        response.result.sqlResult.forEach { it.isFollowing = true }
                        //get profile pic signed urls from either db cache (if they exist + are current pics) or use the new signed urls
                        response.result.sqlResult = getProfilePicSignedUrls(response.result.sqlResult)
                        followingUsersResponse.value = response
                    }
                    followingUsersResponse.value = response})
    }

    fun removeFollowing(cognitoIDUnfollow: String) {
        awsLambdaFunctionCall(false) {
            following.value?.find{it.cognitoId == cognitoIDUnfollow}?.apply {
                isFollowing = false
                isFollowingChangeInProgress = true
            }
            following.notifyObserver()

            val response = followingRepository.removeFollowing(cognitoIDUnfollow)
            if (response.error != null){
                followingUsersResponse.value = AsyncTaskResult(response.error)
                following.value?.find{it.cognitoId == cognitoIDUnfollow}?.run {
                    isFollowing = true
                    isFollowingChangeInProgress = false
                }
                following.notifyObserver()
            } else {
                following.value?.find{it.cognitoId == cognitoIDUnfollow}?.isFollowingChangeInProgress = false
                following.notifyObserver()

                followingUserCacheManager.checkForUpdates()
            }
        }
    }

    fun followUser(cognitoID: String) {
            awsLambdaFunctionCall(false) {
                following.value?.find{it.cognitoId == cognitoID}?.apply {
                    isFollowing = true
                    isFollowingChangeInProgress = true
                }
                following.notifyObserver()


                val response = followingRepository.addFollowingCognitoId(cognitoID)
                if (response.error != null){
                    followingUsersResponse.value = AsyncTaskResult(response.error)
                    following.value?.find{it.cognitoId == cognitoID}?.run {
                        isFollowing = false
                        isFollowingChangeInProgress = false
                    }
                    following.notifyObserver()
                }
                else {
                    following.value?.find{it.cognitoId == cognitoID}?.isFollowingChangeInProgress = false
                    following.notifyObserver()

                    followingUserCacheManager.checkForUpdates()
                }
            }
        }


    fun addFollowing(username: String) {
        awsLambdaFunctionCall(true) {
                    val asyncResult = followingRepository.addFollowing(username)
                    if (asyncResult.error != null){
                        followingUsersResponse.value = AsyncTaskResult(asyncResult.error)
                    }
                    else if (asyncResult.result.error == ResponseFollowing.userNotExistErrorMessage) {
                        followingUsersResponse.value = AsyncTaskResult<ResponseFollowing>(object : CustomError() {
                            override fun getErrorToastMessage(context: Context): String {
                                return context.getString(R.string.username_not_exists_toast)
                            }
                        })
                    }
                    else {
                        //Get the profle pic signed urls to use
                        asyncResult.result?.sqlResult?.let { asyncResult.result.sqlResult = getProfilePicSignedUrls(it.toList())}



                        //Add the added user to the list
                        asyncResult.result?.sqlResult?.forEach {
                            it.isFollowing = true
                            following.value?.add(it)
                        }

                        following.notifyObserver()

                        followingUserCacheManager.checkForUpdates()
                    }
                }
    }


     private suspend fun getProfilePicSignedUrls(userList: List<User>) : List<User>{
        userList.forEach {
            if (it.profilePicCount != 0) {
                if (profilePicMap.containsKey(it.cognitoId)){
                    it.profilePicSignedUrl = profilePicMap[it.cognitoId]
                } else {
                    it.profilePicSignedUrl = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrl(it.cognitoId,  it.profilePicCount , it.profilePicSignedUrl )
                }
            }
        }
        return userList
    }









}
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel used in [ViewFollowingFragment].
 */
class FollowingViewModel @Inject constructor(private val context: Application, private val followingRepository : FollowingRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {

    private val profilePicMap = mutableMapOf<String, String>()

    private val followingUsersResponse = MutableLiveData<AsyncTaskResult<MutableList<User>>>()

    val following = MediatorLiveData<MutableList<User>>()

    val errorMessage : LiveData<String?> = Transformations.map(followingUsersResponse) { asyncResult ->
        if (asyncResult.error != null) {
            getErrorMessage(context, asyncResult.error)
        } else {
            null
        } }


    init {
        following.addSource(followingUsersResponse){
            if (it.error != null) {
               following.value = it.result
            }
        }

        awsLambdaFunctionCall(true,
                suspend {
                    val response = followingRepository.getFollowing()
                    if (response.error == null) {
                        //get profile pic signed urls from either db cache (if they exist + are current pics) or use the new signed urls
                        response.result = getProfilePicSignedUrls(response.result).toMutableList()
                    }
                    followingUsersResponse.value = followingRepository.getFollowing()})
    }

    fun removeFollowing(cognitoIDUnfollow: String) {
        awsLambdaFunctionCall(true,
                suspend {
                    val result = followingRepository.removeFollowing(cognitoIDUnfollow)
                    result.let {
                        //remove user from list
                        followingUsersResponse.value?.result?.remove(followingUsersResponse.value?.result?.find { it.cognitoId == cognitoIDUnfollow })
                        followingUsersResponse.notifyObserver()

                        //Update following user cache
                        FollowingUserCache.get(context, null).updateCache(context, null)
                }}
        )
    }


    fun addFollowing(username: String) {
        awsLambdaFunctionCall(true,
                suspend {
                    val asyncResult = followingRepository.addFollowing(username)

                    if (asyncResult.error != null){
                        followingUsersResponse.value?.error = asyncResult.error
                        followingUsersResponse.notifyObserver()
                    } else {
                        //Get the profle pic signed urls to use
                        asyncResult.result?.sqlResult?.let { asyncResult.result.sqlResult = getProfilePicSignedUrls(it.toList())}

                        //Add the added user to the list
                        asyncResult.result?.sqlResult?.forEach {
                            followingUsersResponse.value?.result?.add(it)
                        }

                        followingUsersResponse.notifyObserver()

                        //Update following user cache
                        FollowingUserCache.get(context, null).updateCache(context, null)
                    }
                }

        )
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
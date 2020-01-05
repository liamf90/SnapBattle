package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import kotlinx.coroutines.async
import javax.inject.Inject

/**
 * The ViewModel used in [FollowFacebookFriendsFragment] and [AddFacebookFriendsAsFollowersStartupFragment].
 */
class AddFacebookFriendsAsFollowersViewModel @Inject constructor(private val context: Application, private val followingRepository : FollowingRepository,
                                                                 private val followingUserCacheManager: FollowingUserCacheManager) : ViewModelLaunch() {

    private val followingUsers = MutableLiveData<AsyncTaskResult<MutableList<User>>>()

    val errorMessage : LiveData<String?> = Transformations.map(followingUsers) { asyncResult ->
        if (asyncResult.error != null){
            getErrorMessage(context.applicationContext, asyncResult.error)
        } else {
            null
        }
    }

    val following = MediatorLiveData<MutableList<User>>()

    init {
        following.addSource(followingUsers){
            if (it.error == null) {
                following.value = it.result
            }
        }

        followingUsers.value = AsyncTaskResult(null)
    }

    fun getFacebookFriends(requestFacebookFriendsPermission : ()->Unit ){
                val facebookFriendsDeferred = viewModelScope.async{followingRepository.getFacebookFriends()}

                awsLambdaFunctionCall(true) {
                    val facebookFriends = facebookFriendsDeferred.await()
                    if (facebookFriends.error != null){
                        followingUsers.value = AsyncTaskResult(facebookFriends.error)
                        return@awsLambdaFunctionCall}

                    //if the user_friends permission has not been approved by the user, the returned reponse will have 0 friends.
                    //so if the response has 0 friends check if the user_friends permission has not been approved and ask the user if they would like to accept it.
                    if (facebookFriends.result.isEmpty() && !doesUserHaveUserFriendsPermission()) {
                        _snackBarMessage.value = context.getString(R.string.need_accept_permission_user_friends)
                        requestFacebookFriendsPermission()
                    } else {
                        followingUsers.value = AsyncTaskResult(facebookFriends.result.toMutableList())
                    }
                }
    }

    fun getFacebookFriendsWithFollowing(requestFacebookFriendsPermission : ()->Unit ){
        val facebookFriendsDeferred = viewModelScope.async{followingRepository.getFacebookFriends()}
        val followingUsersResponseDeferred = viewModelScope.async { followingRepository.getFollowing() }
        awsLambdaFunctionCall(true) {
            val facebookFriends = facebookFriendsDeferred.await()
            val following = followingUsersResponseDeferred.await()
            if (facebookFriends.error == null && following.error == null){
                facebookFriends.result.forEach {
                    val followingUser = following.result.sqlResult.find {u -> u.facebookUserId == it.facebookUserId}
                    if (followingUser != null){
                        it.isFollowing = true
                        it.cognitoId = followingUser.cognitoId
                    }
                }
            } else {
                if (facebookFriends.error != null) followingUsers.value = AsyncTaskResult(facebookFriends.error)
                else if (following.error != null) followingUsers.value = AsyncTaskResult(following.error)
                return@awsLambdaFunctionCall
            }

            //if the user_friends permission has not been approved by the user, the returned reponse will have 0 friends.
            //so if the response has 0 friends check if the user_friends permission has not been approved and ask the user if they would like to accept it.
            if (facebookFriends.result.isEmpty() && !doesUserHaveUserFriendsPermission()) {
                _snackBarMessage.value = context.getString(R.string.need_accept_permission_user_friends)
                requestFacebookFriendsPermission()
            } else {
                followingUsers.value = AsyncTaskResult(facebookFriends.result.toMutableList())
            }
        }
    }



    fun addFollowing(nextFragmentCallback : ()-> Unit) {
        //follow the checked users, if none are checked proceed straight to the next fragment
        val facebookIDListToFollow = following.value?.filter { it.isFollowing }?.mapNotNull { it.facebookUserId }
        if (facebookIDListToFollow != null && facebookIDListToFollow.isNotEmpty()) {
            awsLambdaFunctionCall(true,
                    suspend {
                        val asyncResult = followingRepository.addFollowing(facebookIDListToFollow)
                        if (asyncResult.error != null) {
                            followingUsers.value = AsyncTaskResult(asyncResult.error)
                        } else {
                            followingUserCacheManager.checkForUpdates()

                            //go to nextfragment
                            nextFragmentCallback()
                        }
                    }

            )
        }
    }

    fun removeFollowing(cognitoIDUnfollow: String) {
        awsLambdaFunctionCall(false) {
                following.value?.find { it.cognitoId == cognitoIDUnfollow }?.apply {
                    isFollowing = false
                    isFollowingChangeInProgress = true
                }
                following.notifyObserver()

                val asyncResult = followingRepository.removeFollowing(cognitoIDUnfollow)
                if (asyncResult.error != null){
                    following.value?.find { it.cognitoId == cognitoIDUnfollow }?.run {
                        isFollowing = true
                        isFollowingChangeInProgress = false
                    }
                    following.notifyObserver()
                    followingUsers.value  = AsyncTaskResult(asyncResult.error)
                } else {
                    //unfollow on list
                    following.value?.find { it.cognitoId == cognitoIDUnfollow }?.isFollowingChangeInProgress = false
                    following.notifyObserver()

                    followingUserCacheManager.checkForUpdates()
                    Unit
                 }
                }
    }


    fun addFollowing(facebookUserId: String) {
        awsLambdaFunctionCall(false) {
                    following.value?.find { it.facebookUserId == facebookUserId }?.apply {
                        isFollowing = true
                        isFollowingChangeInProgress = true
                    }
                    following.notifyObserver()

                    val asyncResult = followingRepository.addFollowing(listOf(facebookUserId))
                    if (asyncResult.error != null){
                        following.value?.find { it.facebookUserId == facebookUserId }?.apply {
                            isFollowing = false
                            isFollowingChangeInProgress = false
                        }
                        following.notifyObserver()
                        followingUsers.value = AsyncTaskResult(asyncResult.error)
                    } else {
                            following.value?.find { it.facebookUserId == facebookUserId }?.apply{
                                isFollowingChangeInProgress = false
                                cognitoId = asyncResult.result.sqlResult[0].cognitoId
                            }
                            following.notifyObserver()

                            followingUserCacheManager.checkForUpdates()
                            Unit
                        }
                    }
    }

    private fun doesUserHaveUserFriendsPermission(): Boolean {
        val grantedPermissions =  AccessToken.getCurrentAccessToken().permissions
        return grantedPermissions.contains("user_friends")
    }










}
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import kotlinx.coroutines.async
import javax.inject.Inject

/**
 * The ViewModel used in [FollowFacebookFriendsFragment].
 */
class FollowFacebookFriendsViewModel @Inject constructor(private val context: Application, private val followingRepository : FollowingRepository) : ViewModelLaunch() {

    private val followingUsers = MutableLiveData<AsyncTaskResult<MutableList<User>>>()

    val errorMessage : LiveData<String?> = Transformations.map(followingUsers) { asyncResult ->
        getErrorMessage(context, asyncResult.error)
    }

    val following : LiveData<MutableList<User>>  =  Transformations.map (followingUsers) { asyncResult ->
        asyncResult.result
    }

    init {
        followingUsers.value = AsyncTaskResult(null)
    }

    fun getFacebookFriends(requestFacebookFriendsPermission :()->Unit){

                val facebookFriendsDeferred = viewModelScope.async{followingRepository.getFacebookFriends()}
                val followingUsersDeferred = viewModelScope.async{followingRepository.getFollowing()}

                awsLambdaFunctionCall(true) {
                    val followingUsersResult = followingUsersDeferred.await()
                    if (followingUsersResult.error != null){
                        followingUsers.value?.error = followingUsersResult.error
                        return@awsLambdaFunctionCall
                    }

                    val facebookFriends = facebookFriendsDeferred.await()
                    if (facebookFriends.error != null){
                        followingUsers.value?.error = facebookFriends.error
                        return@awsLambdaFunctionCall}

                    facebookFriends.result.forEach { facebookFriend ->
                        followingUsersResult.result.find { it.facebookUserId == facebookFriend.facebookUserId }.let { facebookFriend.isFollowing = true }
                    }

                    //if the user_friends permission has not been approved by the user, the returned response will have 0 friends.
                    //so if the response has 0 friends check if the user_friends permission has not been approved and ask the user if they would like to accept it.
                    if (facebookFriends.result.isEmpty() && !doesUserHaveUserFriendsPermission()) {
                        _snackBarMessage.value = context.getString(R.string.need_accept_permission_user_friends)
                        requestFacebookFriendsPermission()
                    }

                    followingUsers.value?.result = facebookFriends.result.toMutableList()
                }
    }

    fun removeFollowing(cognitoIDUnfollow: String) {
        awsLambdaFunctionCall(true) {
                suspend {
                    val asyncResult = followingRepository.removeFollowing(cognitoIDUnfollow)
                    if (asyncResult.error != null){
                        followingUsers.value?.error = asyncResult.error
                    } else {
                        //remove user from list
                        followingUsers.value?.result?.remove(followingUsers.value?.result?.find { it.cognitoId == cognitoIDUnfollow })

                        //Update following user cache
                        FollowingUserCache.get(context, null).updateCache(context, null)
                    }}}
    }


    fun addFollowing(username: String) {
        awsLambdaFunctionCall(true,
                suspend {
                    val asyncResult = followingRepository.addFollowing(username)

                    if (asyncResult.error != null){
                        ///change it to like startup
                        followingUsers.value?.error = asyncResult.error
                    } else {
                        //Add the added user to the list
                        asyncResult.result.sqlResult?.forEach {
                            followingUsers.value?.result?.add(it) }

                        //Update following user cache
                        FollowingUserCache.get(context, null).updateCache(context, null)
                    }
                }

        )
    }


    private fun doesUserHaveUserFriendsPermission(): Boolean {
        val declinedPermissions = AccessToken.getCurrentAccessToken().declinedPermissions
        return !declinedPermissions.contains("user_friends")
    }









}
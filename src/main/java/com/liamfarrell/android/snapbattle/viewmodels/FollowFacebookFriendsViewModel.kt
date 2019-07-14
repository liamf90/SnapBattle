package com.liamfarrell.android.snapbattle.viewmodels

import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.amazonaws.AmazonClientException
import com.facebook.AccessToken
import com.facebook.FacebookException
import com.facebook.FacebookRequestError
import com.google.android.gms.tasks.Tasks.await
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache
import com.liamfarrell.android.snapbattle.data.CommentRepository
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Comment
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * The ViewModel used in [FollowFacebookFriendsFragment].
 */
class FollowFacebookFriendsViewModel(val followingRepository : FollowingRepository, val requestFacebookFriendsPermission :()->Unit ) : ViewModelLaunch() {

    private val followingUsers = MutableLiveData<AsyncTaskResult<MutableList<User>>>()

    val errorMessage : LiveData<String?> = Transformations.map(followingUsers) { asyncResult ->
        getErrorMessage(App.getContext(), asyncResult.error)
    }

    val following : LiveData<MutableList<User>>  =  Transformations.map (followingUsers) { asyncResult ->
        asyncResult.result
    }

    init {
        followingUsers.value = AsyncTaskResult(null)
        getFacebookFriends()
    }

    fun getFacebookFriends(){

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

                    //if the user_friends permission has not been approved by the user, the returned reponse will have 0 friends.
                    //so if the response has 0 friends check if the user_friends permission has not been approved and ask the user if they would like to accept it.
                    if (facebookFriends.result.isEmpty() && !doesUserHaveUserFriendsPermission()) {
                        _snackBarMessage.value = App.getContext().getString(R.string.need_accept_permission_user_friends)
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
                        FollowingUserCache.get(App.getContext(), null).updateCache(App.getContext(), null)
                    }}}
    }


    fun addFollowing(username: String) {
        awsLambdaFunctionCall(true,
                suspend {
                    val asyncResult = followingRepository.addFollowing(username)

                    if (asyncResult.error != null){
                        followingUsers.value?.error = asyncResult.error
                    } else {
                        //Add the added user to the list
                        asyncResult.result.sqlResult?.forEach {
                            followingUsers.value?.result?.add(it) }

                        //Update following user cache
                        FollowingUserCache.get(App.getContext(), null).updateCache(App.getContext(), null)
                    }
                }

        )
    }


    private fun doesUserHaveUserFriendsPermission(): Boolean {
        val declinedPermissions = AccessToken.getCurrentAccessToken().declinedPermissions
        return !declinedPermissions.contains("user_friends")
    }









}
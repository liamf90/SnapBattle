package com.liamfarrell.android.snapbattle.viewmodels.startup

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch
import kotlinx.coroutines.async
import javax.inject.Inject

/**
 * The ViewModel used in [FollowFacebookFriendsFragment].
 */
class AddFacebookFriendsAsFollowersStartupViewModel @Inject constructor(private val context: Application, private val followingRepository : FollowingRepository) : ViewModelLaunch() {

    private val followingUsers = MutableLiveData<AsyncTaskResult<MutableList<User>>>()

    val errorMessage : LiveData<String?> = Transformations.map(followingUsers) { asyncResult ->
        if (asyncResult.error != null){
            getErrorMessage(context, asyncResult.error)
        } else {
            null
        }
    }

    val following : LiveData<MutableList<User>>  =  Transformations.map (followingUsers) { asyncResult ->
        asyncResult.result
    }

    init {
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

    fun addFollowing(nextFragmentCallback : ()-> Unit) {
        //follow the checked users, if none are checked proceed straight to the next fragment
        val facebookIDListToFollow = following.value?.filter { it.isFollowing }?.mapNotNull { it.facebookUserId }
        if (facebookIDListToFollow != null && facebookIDListToFollow.isNotEmpty()) {
            awsLambdaFunctionCall(true,
                    suspend {
                        val asyncResult = followingRepository.addFollowing(facebookIDListToFollow)
                        if (asyncResult.error != null) {
                            followingUsers.value?.error = asyncResult.error
                        } else {
                            //GO TO NEXT FRAGMENT
                            nextFragmentCallback()
                        }
                    }

            )
        }
    }


    private fun doesUserHaveUserFriendsPermission(): Boolean {
        val declinedPermissions = AccessToken.getCurrentAccessToken().declinedPermissions
        return !declinedPermissions.contains("user_friends")
    }









}
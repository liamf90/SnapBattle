package com.liamfarrell.android.snapbattle.viewmodels

import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.amazonaws.AmazonClientException
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
 * The ViewModel used in [ViewFollowingFragment].
 */
class FollowingViewModel(val followingRepository : FollowingRepository ) : ViewModelLaunch() {

    private val followingUsers = MutableLiveData<AsyncTaskResult<MutableList<User>>>()


    val following : LiveData<MutableList<User>>  =  Transformations.map (followingUsers) { asyncResult ->
        asyncResult.result }

    val errorMessage : LiveData<String?> = Transformations.map(followingUsers) { asyncResult ->
        getErrorMessage(App.getContext(), asyncResult.error) }



    init {
        awsLambdaFunctionCall(true,
                suspend {followingUsers.value = followingRepository.getFollowing()})
    }

    fun removeFollowing(cognitoIDUnfollow: String) {
        awsLambdaFunctionCall(true,
                suspend {
                    val result = followingRepository.removeFollowing(cognitoIDUnfollow)
                    result.let {
                        //remove user from list
                        followingUsers.value?.result?.remove(followingUsers.value?.result?.find { it.cognitoId == cognitoIDUnfollow })

                        //Update following user cache
                        FollowingUserCache.get(App.getContext(), null).updateCache(App.getContext(), null)
                }}
        )
    }


    fun addFollowing(username: String) {
        awsLambdaFunctionCall(true,
                suspend {
                    val asyncResult = followingRepository.addFollowing(username)

                    if (asyncResult.error != null){
                        followingUsers.value?.error = asyncResult.error
                    } else {

                        //Add the added user to the list
                        asyncResult.result?.sqlResult?.forEach {
                            followingUsers.value?.result?.add(it)
                        }

                        //Update following user cache
                        FollowingUserCache.get(App.getContext(), null).updateCache(App.getContext(), null)
                    }
                }

        )
    }









}
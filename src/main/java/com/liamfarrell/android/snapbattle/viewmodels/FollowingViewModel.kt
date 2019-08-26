package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.caches.FollowingUserCache
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject

/**
 * The ViewModel used in [ViewFollowingFragment].
 */
class FollowingViewModel @Inject constructor(private val context: Application, private val followingRepository : FollowingRepository ) : ViewModelLaunch() {

    private val followingUsers = MutableLiveData<AsyncTaskResult<MutableList<User>>>()


    val following : LiveData<MutableList<User>>  =  Transformations.map (followingUsers) { asyncResult ->
        asyncResult.result }

    val errorMessage : LiveData<String?> = Transformations.map(followingUsers) { asyncResult ->
        if (asyncResult.error != null) {
            getErrorMessage(context, asyncResult.error)
        } else {
            null
        } }



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
                        FollowingUserCache.get(context, null).updateCache(context, null)
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
                        FollowingUserCache.get(context, null).updateCache(context, null)
                    }
                }

        )
    }









}
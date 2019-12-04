package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.coroutines.async
import okhttp3.Response
import javax.inject.Inject

/**
 * The ViewModel used in [FollowFacebookFriendsFragment] and [AddFacebookFriendsAsFollowersStartupFragment].
 */
class AddFacebookFriendsAsFollowersViewModel @Inject constructor(private val context: Application, private val followingRepository : FollowingRepository,
                                                                 private val followingUserCacheManager: FollowingUserCacheManager) : ViewModelLaunch() {

    private val _following = MutableLiveData<MutableList<User>>()
    val following : LiveData<MutableList<User>> = _following

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }




    fun getFacebookFriends(requestFacebookFriendsPermission : ()->Unit ){
        compositeDisposable.add(  followingRepository.getFacebookFriendsRx()
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { facebookFriendsList ->
                            _spinner.value = false
                            //if the user_friends permission has not been approved by the user, the returned reponse will have 0 friends.
                            //so if the response has 0 friends check if the user_friends permission has not been approved and ask the user if they would like to accept it.
                            if (facebookFriendsList.isEmpty() && !doesUserHaveUserFriendsPermission()) {
                                _snackBarMessage.value = context.getString(R.string.need_accept_permission_user_friends)
                                requestFacebookFriendsPermission()
                            } else {
                                _following.value = facebookFriendsList.toMutableList()
                            }
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }


    fun getFacebookFriendsWithFollowing(requestFacebookFriendsPermission : ()->Unit) {
        compositeDisposable.add(  Single.zip(followingRepository.getFacebookFriendsRx().subscribeOn(io()),
                followingRepository.getFollowingRx().subscribeOn(io()),
                BiFunction<List<User>, ResponseFollowing, List<User>> {
                    facebookFriends : List<User>, following : ResponseFollowing ->
                        facebookFriends.forEach {
                            val followingUser = following.sqlResult.find { u -> u.facebookUserId == it.facebookUserId }
                            if (followingUser != null) {
                                it.isFollowing = true
                                it.cognitoId = followingUser.cognitoId
                            }
                        }
                    return@BiFunction facebookFriends
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            //if the user_friends permission has not been approved by the user, the returned reponse will have 0 friends.
                            //so if the response has 0 friends check if the user_friends permission has not been approved and ask the user if they would like to accept it.
                            if (onSuccessResponse.isEmpty() && !doesUserHaveUserFriendsPermission()) {
                                _snackBarMessage.value = context.getString(R.string.need_accept_permission_user_friends)
                                requestFacebookFriendsPermission()
                            } else {
                                _following.value = onSuccessResponse.toMutableList()
                            }
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }


    fun addFollowing(nextFragmentCallback : ()-> Unit) {
        //follow the checked users, if none are checked proceed straight to the next fragment
        val facebookIDListToFollow = following.value?.filter { it.isFollowing }?.mapNotNull { it.facebookUserId }
        if (facebookIDListToFollow != null && facebookIDListToFollow.isNotEmpty()) {
            compositeDisposable.add( followingRepository.addFollowingRx(facebookIDListToFollow)
                    .subscribeOn(io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe{_spinner.value = true}
                    .subscribe(
                            { _ ->
                                _spinner.value = false
                                //TODO followingUserCacheManager.checkForUpdates()

                                //go to next fragment
                                nextFragmentCallback()
                            },
                            { onError : Throwable ->
                                _spinner.value = false
                                error.value = onError
                            }
                    ))
        }
    }

    fun removeFollowing(cognitoIDUnfollow: String) {
        compositeDisposable.add( followingRepository.removeFollowingRx(cognitoIDUnfollow)
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{
                    _following.value?.find { it.cognitoId == cognitoIDUnfollow }?.apply {
                        isFollowing = false
                        isFollowingChangeInProgress = true
                    }
                    _following.notifyObserver()
                }
                .subscribe(
                        { _ ->
                            //unfollow on list
                            _following.value?.find { it.cognitoId == cognitoIDUnfollow }?.isFollowingChangeInProgress = false
                            _following.notifyObserver()

                            //TODO followingUserCacheManager.checkForUpdates()
                        },
                        { onError : Throwable ->
                            _following.value?.find { it.cognitoId == cognitoIDUnfollow }?.run {
                                isFollowing = true
                                isFollowingChangeInProgress = false
                            }
                            _following.notifyObserver()
                            error.value = onError
                        }
                ))
    }


    fun addFollowing(facebookUserId: String) {
        compositeDisposable.add(  followingRepository.addFollowingRx(listOf(facebookUserId))
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{
                    _following.value?.find { it.facebookUserId == facebookUserId }?.apply {
                        isFollowing = true
                        isFollowingChangeInProgress = true
                    }
                    _following.notifyObserver()
                }
                .subscribe(
                        { response ->
                            _following.value?.find { it.facebookUserId == facebookUserId }?.apply{
                                isFollowingChangeInProgress = false
                                cognitoId = response.sqlResult[0].cognitoId
                            }
                            _following.notifyObserver()

                            //TODO followingUserCacheManager.checkForUpdates()
                            Unit
                        },
                        { onError : Throwable ->
                            _following.value?.find { it.facebookUserId == facebookUserId }?.apply {
                                isFollowing = false
                                isFollowingChangeInProgress = false
                            }
                            _following.notifyObserver()
                            error.value = onError
                        }
                ))

    }



    private fun doesUserHaveUserFriendsPermission(): Boolean {
        val declinedPermissions = AccessToken.getCurrentAccessToken().declinedPermissions
        return !declinedPermissions.contains("user_friends")
    }









}
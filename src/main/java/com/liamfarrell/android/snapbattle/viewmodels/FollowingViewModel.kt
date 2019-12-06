package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Comment
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * The ViewModel used in [ViewFollowingFragment].
 */
class FollowingViewModel @Inject constructor(private val context: Application, private val followingRepository : FollowingRepository,
                                             private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository, private val followingUserCacheManager: FollowingUserCacheManager) : ViewModelLaunch() {

    private val profilePicMap = mutableMapOf<String, String>()

    private val _following = MutableLiveData<MutableList<User>>()
    val following : LiveData<MutableList<User>> = _following

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }



    init {
        getFollowing()
    }

    private fun getFollowing(){
        compositeDisposable.add(  followingRepository.getFollowingRx()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe{_spinner.value = true}
                .map { getProfilePicSignedUrls(it.sqlResult).toMutableList() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            //set all users as following
                            onSuccessResponse.forEach { it.isFollowing = true }
                            //get profile pic signed urls from either db cache (if they exist + are current pics) or use the new signed urls
                            _following.value = onSuccessResponse
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    fun removeFollowing(cognitoIDUnfollow: String) {
        compositeDisposable.add(  followingRepository.removeFollowingRx(cognitoIDUnfollow)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{
                    _following.value?.find{it.cognitoId == cognitoIDUnfollow}?.apply {
                        isFollowing = false
                        isFollowingChangeInProgress = true
                    }
                    _following.notifyObserver() }
                .subscribe(
                        { onSuccessResponse ->
                            _following.value?.find{it.cognitoId == cognitoIDUnfollow}?.isFollowingChangeInProgress = false
                            _following.notifyObserver()

                            //TODO followingUserCacheManager.checkForUpdates()
                        },
                        {onError : Throwable ->
                            error.value = onError
                            _following.value?.find{it.cognitoId == cognitoIDUnfollow}?.run {
                                isFollowing = true
                                isFollowingChangeInProgress = false
                            }
                            _following.notifyObserver()
                        }
                ))
    }

    fun followUser(cognitoID: String) {

        compositeDisposable.add(  followingRepository.addFollowingCognitoIdRx(cognitoID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{
                    _following.value?.find{it.cognitoId == cognitoID}?.apply {
                        isFollowing = true
                        isFollowingChangeInProgress = true
                    }
                    _following.notifyObserver() }
                .subscribe(
                        { onSuccessResponse ->
                            _following.value?.find{it.cognitoId == cognitoID}?.isFollowingChangeInProgress = false
                            _following.notifyObserver()

                            //TODO followingUserCacheManager.checkForUpdates()
                        },
                        {onError : Throwable ->
                            error.value = onError
                            _following.value?.find{it.cognitoId == cognitoID}?.run {
                                isFollowing = false
                                isFollowingChangeInProgress = false
                            }
                            _following.notifyObserver()
                        }
                ))
        }


    fun addFollowing(username: String) {
        compositeDisposable.add(  followingRepository.addFollowingRx(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            //Get the profle pic signed urls to use
                            onSuccessResponse.sqlResult?.let { onSuccessResponse.sqlResult = getProfilePicSignedUrls(it.toList())}

                            //Add the added user to the list
                            onSuccessResponse.sqlResult?.forEach {
                                _following.value?.add(it)
                            }
                            _following.notifyObserver()

                            //TODO followingUserCacheManager.checkForUpdates()
                        },
                        { onError : Throwable ->
                            error.value = onError
                        }
                ))
    }


     private fun getProfilePicSignedUrls(userList: List<User>) : List<User>{
        userList.forEach {
            if (it.profilePicCount != 0) {
                if (profilePicMap.containsKey(it.cognitoId)){
                    it.profilePicSignedUrl = profilePicMap[it.cognitoId]
                } else {
                    it.profilePicSignedUrl = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrlRx(it.cognitoId,  it.profilePicCount , it.profilePicSignedUrl )
                }
            }
        }
        return userList
    }









}
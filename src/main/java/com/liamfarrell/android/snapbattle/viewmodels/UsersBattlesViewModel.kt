package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.data.UsersBattlesRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel used in [UsersBattlesFragment].
 */
class UsersBattlesViewModel @Inject constructor(private val context: Application, private val usersBattlesRepository: UsersBattlesRepository
                                                ,  private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository, val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelLaunch() {

    private lateinit var cognitoId : String

    private val _battles = MutableLiveData<MutableList<Battle>>()
    val battles : LiveData<MutableList<Battle>> = _battles

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }

    private val _user = MutableLiveData<User>()
    val user : LiveData<User> = _user




    fun setCognitoId(cognitoID: String){
        cognitoId = cognitoID

        compositeDisposable.add(usersBattlesRepository.getUsersBattlesRx(cognitoID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe{_spinner.value = true}
                .map {
                    //check if profile pic url in cache else use new signed url
                    val profile = it.user_profile
                    if (profile.profilePicCount > 0) {
                        it.user_profile.profilePicSignedUrl =  otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrlRx(profile.cognitoId,  profile.profilePicCount , profile.profilePicSignedUrl )
                    }
                    it.user_battles = getThumbnailSignedUrls(it.user_battles)
                    it.user_battles.filter {!it.isDeleted}
                    return@map it
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            _battles.value = onSuccessResponse.user_battles
                            _user.value = onSuccessResponse.user_profile
                        },
                        {onError : Throwable ->
                            onError.printStackTrace()
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    fun setFacebookId(facebookId: String){
        compositeDisposable.add( usersBattlesRepository.getUsersBattlesWithFacebookIdRx(facebookId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe{_spinner.value = true}
                .map {
                    //check if profile pic url in cache else use new signed url
                    val profile = it.user_profile
                    if (profile.profilePicCount > 0) {
                        it.user_profile.profilePicSignedUrl =  otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrlRx(profile.cognitoId,  profile.profilePicCount , profile.profilePicSignedUrl )
                    }
                    it.user_battles = getThumbnailSignedUrls(it.user_battles)
                    it.user_battles.filter {!it.isDeleted}
                    return@map it
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            cognitoId = onSuccessResponse.user_profile.cognitoId
                            _battles.value = onSuccessResponse.user_battles
                            _user.value = onSuccessResponse.user_profile
                        },
                        {onError : Throwable ->
                            onError.printStackTrace()
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    fun followUser(){
        compositeDisposable.add(  usersBattlesRepository.followUserRx(cognitoId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe {
                    _user.value?.isFollowingChangeInProgress = true
                    _user.value?.isFollowing = true
                    _user.notifyObserver()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            _user.value?.isFollowingChangeInProgress = false
                            _user.notifyObserver()
                        },
                        {onError : Throwable ->
                            _user.value?.isFollowingChangeInProgress = false
                            _user.value?.isFollowing = false
                            _user.notifyObserver()
                            error.value = onError
                        }
                ))
    }

    fun unfollowUser(){
        compositeDisposable.add(  usersBattlesRepository.unfollowUserRx(cognitoId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe {
                    _user.value?.isFollowingChangeInProgress = true
                    _user.value?.isFollowing = false
                    _user.notifyObserver()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            _user.value?.isFollowingChangeInProgress = false
                            _user.notifyObserver()
                        },
                        {onError : Throwable ->
                            _user.value?.isFollowingChangeInProgress = false
                            _user.value?.isFollowing = true
                            _user.notifyObserver()
                            error.value = onError
                        }
                ))
    }

    private fun getThumbnailSignedUrls(battleList: List<Battle>) : List<Battle>{
        battleList.forEach {
            it.signedThumbnailUrl = thumbnailSignedUrlCacheRepository.getThumbnailSignedUrlRx(it)
        }
        return battleList
    }


}
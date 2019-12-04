package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.CompletedBattlesRepository
import com.liamfarrell.android.snapbattle.data.CurrentBattlesRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel used in [BattleCurrentListFragment].
 */

@OpenForTesting
class CurrentBattlesViewModel @Inject constructor(private val context: Application, private val identityManager: IdentityManager, private val currentBattlesRepository: CurrentBattlesRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {


    private val _battles = MutableLiveData<MutableList<Battle>>()
    val battles : LiveData<MutableList<Battle>> = _battles

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }


    init {
        getCurrentBattles()
    }

    private fun getCurrentBattles(){
        compositeDisposable.add(currentBattlesRepository.getCurrentBattlesRxJava()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe{_spinner.value = true}
                .map { getProfilePicSignedUrls(it.sqlResult).toMutableList() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            _battles.value = onSuccessResponse
                        },
                        {onError : Throwable ->
                            onError.printStackTrace()
                            _spinner.value = false
                            error.value = onError
                        }
                ))

    }


     private fun getProfilePicSignedUrls(battleList: List<Battle>) : List<Battle>{
        val currentCognitoId = identityManager.cachedUserID
        val cognitoIdList = battleList.distinctBy { it.getOpponentCognitoID(currentCognitoId)}
        val signedUrlMap = mutableMapOf<String, String>()
        cognitoIdList.forEach {
            val signedUrlToUse = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrlRx(it.getOpponentCognitoID(currentCognitoId),  it.getOpponentProfilePicCount(currentCognitoId), it.profilePicSmallSignedUrl)
            signedUrlMap[it.getOpponentCognitoID(currentCognitoId)] = signedUrlToUse
        }
        battleList.forEach {
            it.profilePicSmallSignedUrl = signedUrlMap[it.getOpponentCognitoID(currentCognitoId)]
        }
        return battleList
    }


}
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.navigation.NavController
import com.liamfarrell.android.snapbattle.data.BattleChallengesRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.BattleChallengesListFragmentDirections
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject
import io.reactivex.schedulers.Schedulers.io
import com.liamfarrell.android.snapbattle.util.notifyObserver
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers



/**
 * The ViewModel used in [BattleChallengesListFragment].
 */
class BattleChallengesViewModel @Inject constructor(private val context: Application, private val battleChallengesRepository: BattleChallengesRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {

    private val _battles = MutableLiveData<MutableList<Battle>>()
    val battles : LiveData<MutableList<Battle>> = _battles

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }



    init {
        loadChallenges()
    }

    private final fun loadChallenges(){
        compositeDisposable.add(battleChallengesRepository.getBattleChallengesRxJava()
                .subscribeOn(io())
                .observeOn(io())
                .doOnSubscribe{_spinner.value = true}
                .map { getProfilePicSignedUrls(it.sql_result).toMutableList() }
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


    fun onBattleAccepted(navController: NavController, battle: Battle) {
        compositeDisposable.add(battleChallengesRepository.updateBattleAcceptedRxJava (true, battle.battleId)
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        {
                            _battles.value?.remove(battle)
                            _battles.notifyObserver()
                            val direction = BattleChallengesListFragmentDirections.actionBattleChallengesListFragmentToViewBattleFragment(battle.battleId)
                            navController.navigate(direction)},
                        {onError ->
                            error.value = onError
                        }
                ))
    }

    fun onBattleDeclined(battle: Battle) {
        compositeDisposable.add(battleChallengesRepository.updateBattleAcceptedRxJava (false, battle.battleId)
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        {
                            _battles.value?.remove(battle)
                            _battles.notifyObserver() },
                        {onError ->
                            error.value = onError
                        }
                ))

    }




    private  fun getProfilePicSignedUrls(battleList: List<Battle>) : List<Battle>{

        val currentCognitoId = com.amazonaws.mobile.auth.core.IdentityManager.getDefaultIdentityManager().cachedUserID
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
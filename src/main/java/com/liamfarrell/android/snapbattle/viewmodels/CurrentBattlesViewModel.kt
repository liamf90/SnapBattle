package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.CompletedBattlesRepository
import com.liamfarrell.android.snapbattle.data.CurrentBattlesRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel used in [BattleCurrentListFragment].
 */
class CurrentBattlesViewModel @Inject constructor(private val context: Application, private val currentBattlesRepository: CurrentBattlesRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {


    private val battlesResult = MutableLiveData<AsyncTaskResult<CurrentBattleResponse>>()

    val errorMessage = MediatorLiveData<String>()
    val battles = MediatorLiveData<List<Battle>>()


    init {
        errorMessage.addSource(battlesResult) { result ->
             if (result.error != null){
                getErrorMessage(context.applicationContext, result.error)}
            else if (result.result.sqlResult.size == 0){
                context.getString(R.string.no_current_battles_toast)}
        }

        battles.addSource(battlesResult) { result ->
                if (result.error == null){
                   battles.value = result.result.sqlResult }
        }

        awsLambdaFunctionCall(true,
                suspend {
                    val response = currentBattlesRepository.getCurrentBattles()
                    if (response.error == null) {
                        //get profile pic signed urls from either db cache (if they exist + are current pics) or use the new signed urls
                        response.result.sqlResult = getProfilePicSignedUrls(response.result.sqlResult)
                    }
                    battlesResult.value = response
                })
       }


    private suspend fun getProfilePicSignedUrls(battleList: List<Battle>) : List<Battle>{
        val currentCognitoId = com.amazonaws.mobile.auth.core.IdentityManager.getDefaultIdentityManager().cachedUserID
        val cognitoIdList = battleList.distinctBy { it.getOpponentCognitoID(currentCognitoId)}
        val signedUrlMap = mutableMapOf<String, String>()
        cognitoIdList.forEach {
            val signedUrlToUse = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrl(it.getOpponentCognitoID(currentCognitoId),  it.getOpponentProfilePicCount(currentCognitoId), it.profilePicSmallSignedUrl)
            signedUrlMap[it.getOpponentCognitoID(currentCognitoId)] = signedUrlToUse
        }
        battleList.forEach {
            it.profilePicSmallSignedUrl = signedUrlMap[it.getOpponentCognitoID(currentCognitoId)]
        }
        return battleList
    }


}
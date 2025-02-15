package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.liamfarrell.android.snapbattle.data.CurrentBattlesRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject
import kotlin.collections.List
import kotlin.collections.distinctBy
import kotlin.collections.forEach
import kotlin.collections.mutableMapOf
import kotlin.collections.set

/**
 * The ViewModel used in [BattleCurrentListFragment].
 */

@OpenForTesting
class CurrentBattlesViewModel @Inject constructor(private val context: Application, private val awsMobileClient: AWSMobileClient, private val currentBattlesRepository: CurrentBattlesRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelBase() {


    private val battlesResult = MutableLiveData<AsyncTaskResult<CurrentBattleResponse>>()

    val errorMessage = MediatorLiveData<String>()
    val battles = MediatorLiveData<List<Battle>>()


    init {
        errorMessage.addSource(battlesResult) { result ->
             if (result.error != null){
                 errorMessage.value = getErrorMessage(context.applicationContext, result.error)}
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
        val currentCognitoId = awsMobileClient.identityId
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
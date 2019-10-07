package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.BattleChallengesRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel used in [BattleChallengesListFragment].
 */
class BattleChallengesViewModel @Inject constructor(private val context: Application, private val battleChallengesRepository: BattleChallengesRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {

    private val battlesResponse = MutableLiveData<AsyncTaskResult<GetChallengesResponse>>()

    val battles = MediatorLiveData<MutableList<Battle>>()

    val errorMessage = MediatorLiveData<String>()


    init {
        battles.addSource(battlesResponse) { result ->
                if (result.error == null){
                    battles.value = result.result.sql_result
                }
        }


        errorMessage.addSource(battlesResponse) { result ->
            if (result.error != null){
                errorMessage.value = getErrorMessage(context, result.error)}
            else if (result.result.sql_result.size == 0){
                errorMessage.value =  context.getString(R.string.no_challenges_battles_toast)}
        }

        awsLambdaFunctionCall(true,
                suspend {
                    val response = battleChallengesRepository.getBattleChallenges()
                    if (response.error == null) {
                        //get profile pic signed urls from either db cache (if they exist + are current pics) or use the new signed urls
                        response.result.sql_result = getProfilePicSignedUrls(response.result.sql_result)
                    }
                    battlesResponse.value = battleChallengesRepository.getBattleChallenges()

                })
    }

    fun onBattleAccepted(battle: Battle) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = battleChallengesRepository.updateBattleAccepted(true, battle.battleId)
                    if (response.error != null){
                        battlesResponse.value = AsyncTaskResult(response.error)
                    } else{
                        battles.value?.remove(battle)
                        battlesResponse.value = AsyncTaskResult(GetChallengesResponse().apply {setSqlResult(battles.value)})
                    }
                    //TODO GO TO BATTLE PAGE
                    Unit
                })
    }

    fun onBattleDeclined(battle: Battle) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = battleChallengesRepository.updateBattleAccepted(false, battle.battleId)
                    if (response.error != null){
                        battlesResponse.value = AsyncTaskResult(response.error)
                    } else{
                        battles.value?.remove(battle)
                        battlesResponse.value = AsyncTaskResult(GetChallengesResponse().apply {setSqlResult(battles.value)})
                    }
                    Unit
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
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.amazonaws.mobile.client.AWSMobileClient
import com.liamfarrell.android.snapbattle.data.BattleChallengesRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.BattleChallengesListFragmentDirections
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.distinctBy
import kotlin.collections.forEach
import kotlin.collections.mutableMapOf
import kotlin.collections.set


/**
 * The ViewModel used in [BattleChallengesListFragment].
 */
class BattleChallengesViewModel @Inject constructor(private val context: Application, private val battleChallengesRepository: BattleChallengesRepository, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository,
                                                    private val awsMobileClient: AWSMobileClient) : ViewModelBase() {

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
        }

        loadChallenges()
    }

    private final fun loadChallenges(){
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

    fun onBattleAccepted(navController: NavController, battle: Battle) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = battleChallengesRepository.updateBattleAccepted(true, battle.battleId)
                    if (response.error != null){
                        battlesResponse.value = AsyncTaskResult(response.error)
                    } else{
                        battles.value?.remove(battle)
                        battlesResponse.value = AsyncTaskResult(GetChallengesResponse().apply {setSqlResult(battles.value)})

                        val direction = BattleChallengesListFragmentDirections.actionBattleChallengesListFragmentToViewBattleFragment(battle.battleId)
                        navController.navigate(direction)
                    }

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
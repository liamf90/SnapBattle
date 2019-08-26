package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.BattleChallengesRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject

/**
 * The ViewModel used in [BattleChallengesListFragment].
 */
class BattleChallengesViewModel @Inject constructor(private val context: Application, private val battleChallengesRepository: BattleChallengesRepository) : ViewModelLaunch() {

    private val battlesResponse = MutableLiveData<AsyncTaskResult<GetChallengesResponse>>()

    val battles : LiveData<List<Battle>> =  Transformations.map(battlesResponse) { asyncResult ->

        asyncResult.result.sql_result }

    val errorMessage : LiveData<String?> = Transformations.map(battlesResponse) { asyncResult ->
        if (asyncResult.error != null){
            getErrorMessage(context, asyncResult.error)}
        else if (asyncResult.result.sql_result.size == 0){
            context.getString(R.string.no_challenges_battles_toast)}
        else
        null
    }


    init {
        awsLambdaFunctionCall(true,
                suspend {battlesResponse.value = battleChallengesRepository.getBattleChallenges()})
    }

    fun onBattleAccepted(battle: Battle) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = battleChallengesRepository.updateBattleAccepted(true, battle.battleId)
                    if (response.error != null){
                        battlesResponse.value?.error = response.error
                    } else{
                        battlesResponse.value?.result?.sql_result?.remove(battle)
                    }
                    //TODO GO TO BATTLE PAGE
                    Unit
                })
    }

    fun onBattleDeclined(battle: Battle) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = battleChallengesRepository.updateBattleAccepted(true, battle.battleId)
                    if (response.error != null){
                        battlesResponse.value?.error = response.error
                    } else{
                        battlesResponse.value?.result?.sql_result?.remove(battle)
                    }
                    Unit
                })
    }

}
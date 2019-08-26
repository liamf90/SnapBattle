package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.data.CompletedBattlesRepository
import com.liamfarrell.android.snapbattle.data.CurrentBattlesRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject

/**
 * The ViewModel used in [BattleCurrentListFragment].
 */
class CurrentBattlesViewModel @Inject constructor(private val context: Application, private val currentBattlesRepository: CurrentBattlesRepository ) : ViewModelLaunch() {


    private val battlesResult = MutableLiveData<AsyncTaskResult<CurrentBattleResponse>>()

    val errorMessage = MediatorLiveData<String>()
    val battles = MediatorLiveData<List<Battle>>()


    init {
        errorMessage.addSource(battlesResult) { result ->
            result?.let { errorMessage.value = getErrorMessage(context, result.error) }
        }

        battles.addSource(battlesResult) { result ->
            result?.let { battles.value = result.result.sqlResult }
        }

        awsLambdaFunctionCall(true,
                suspend {battlesResult.value = currentBattlesRepository.getCurrentBattles()})
    }
}
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseBattle
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject

/**
 * The ViewModel used in [ViewOwnBattleFragment].
 */
class ViewOwnBattleViewModel @Inject constructor(private val context: Application, private val usersBattleRepository: UsersBattleRepository) : ViewModelBase() {

    private val battleResult = MutableLiveData<AsyncTaskResult<ResponseBattle>>()

    val errorMessage : LiveData<String?> = Transformations.map(battleResult) { asyncResult ->
        asyncResult.error?.let{getErrorMessage(context, asyncResult.error)}
    }

    val battle : LiveData<Battle> =  Transformations.map(battleResult) { asyncResult ->
        asyncResult.result.sqlResult
    }


    fun getBattle(battleID : Int){
        awsLambdaFunctionCall(battle.value == null,
                suspend {battleResult.value = usersBattleRepository.getBattle(battleID)})
    }



}
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject

/**
 * The ViewModel used in [ViewOwnBattleFragment].
 */
class ViewOwnBattleViewModel @Inject constructor(private val context: Application, private val usersBattleRepository: UsersBattleRepository) : ViewModelLaunch() {

    private val battleResult = MutableLiveData<AsyncTaskResult<Battle>>()

    val errorMessage : LiveData<String?> = Transformations.map(battleResult) { asyncResult ->
        asyncResult.error?.let{getErrorMessage(context, asyncResult.error)}
    }

    val battle : LiveData<Battle> =  Transformations.map(battleResult) { asyncResult ->
        asyncResult.result
    }




    fun getBattle(battleID : Int){
        awsLambdaFunctionCall(true,
                suspend {battleResult.value = usersBattleRepository.getBattle(battleID)})
    }



}
package com.liamfarrell.android.snapbattle.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.data.CompletedBattlesRepository
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Video
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The ViewModel used in [ViewOwnBattleFragment].
 */
class ViewOwnBattleViewModel(val battleID : Int, val usersBattleRepository: UsersBattleRepository) : ViewModelLaunch() {

    private val battleResult = MutableLiveData<AsyncTaskResult<Battle>>()

    val errorMessage : LiveData<String?> = Transformations.map(battleResult) { asyncResult ->
        getErrorMessage(App.getContext(), asyncResult.error)
    }

    val battle : LiveData<Battle> =  Transformations.map(battleResult) { asyncResult ->
        asyncResult.result
    }




    init {
            getBattle()
    }

    fun getBattle(){
        awsLambdaFunctionCall(true,
                suspend {battleResult.value = usersBattleRepository.getBattle(battleID)})
    }



}
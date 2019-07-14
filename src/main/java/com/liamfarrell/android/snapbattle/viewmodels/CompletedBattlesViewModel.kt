package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.data.CompletedBattlesRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Comment
import com.liamfarrell.android.snapbattle.util.getErrorMessage


/**
 * The ViewModel used in [BattleCompletedListFragment].
 */
class CompletedBattlesViewModel(val completedBattlesRepository: CompletedBattlesRepository ) : ViewModelLaunch() {

    private val battlesResult = MutableLiveData<AsyncTaskResult<List<Battle>>>()

    val errorMessage : LiveData<String?> = Transformations.map(battlesResult) { result ->
        getErrorMessage(App.getContext(), result.error)
    }

    val battles : LiveData<List<Battle>>  =  Transformations.map (battlesResult) { result ->
        result.result
    }


    init {
        awsLambdaFunctionCall(true,
                suspend {battlesResult.value = completedBattlesRepository.getCompletedBattles()})
    }
}
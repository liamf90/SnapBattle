package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.data.CompletedBattlesRepository
import com.liamfarrell.android.snapbattle.data.CurrentBattlesRepository
import com.liamfarrell.android.snapbattle.model.Battle

/**
 * The ViewModel used in [BattleCompletedListFragment].
 */
class CurrentBattlesViewModel(val currentBattlesRepository: CurrentBattlesRepository ) : ViewModelLaunch() {

    val battles = MutableLiveData<List<Battle>>()



    init {
        awsLambdaFunctionCall(true,
                suspend {battles.value = currentBattlesRepository.getCurrentBattles().result.sqlResult})
    }
}
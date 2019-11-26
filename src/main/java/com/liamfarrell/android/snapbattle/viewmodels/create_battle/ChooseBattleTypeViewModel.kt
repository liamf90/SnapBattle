package com.liamfarrell.android.snapbattle.viewmodels.create_battle

import android.app.Application
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.ChooseBattleTypeRepository
import com.liamfarrell.android.snapbattle.data.TopBattlesRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.pjos.RecentBattleNamePOJO
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.RecentBattleResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch
import javax.inject.Inject

/**
 * The ViewModel used in [BattleCompletedListFragment].
 */
class ChooseBattleTypeViewModel @Inject constructor(private val context: Application, private val chooseBattleTypeRepository: ChooseBattleTypeRepository, private val topBattlesRepository: TopBattlesRepository) : ViewModelLaunch() {

    val battleName = MutableLiveData<String>()

    private val topBattlesResponse = MutableLiveData<List<String>>()
    val topBattles : LiveData<List<String>> = topBattlesResponse

    private val _topBattlesLoading = MutableLiveData<Boolean>()
    val topBattlesLoading : LiveData<Boolean> = _topBattlesLoading


    private val recentBattlesResponse = MutableLiveData<AsyncTaskResult<RecentBattleResponse>>()
    val recentBattles : LiveData<List<RecentBattleNamePOJO>> =  Transformations.map(recentBattlesResponse) { asyncResult ->
        asyncResult.result.sqlResult }

    private val _recentBattlesLoading = MutableLiveData<Boolean>()
    val recentBattlesLoading : LiveData<Boolean> = _recentBattlesLoading


    private val battleNameSearchResponse = MutableLiveData<AsyncTaskResult<BattleTypeSuggestionsSearchResponse>>()
    val battleNameSearchList : LiveData<List<SuggestionsResponse>> =  Transformations.map(battleNameSearchResponse) { asyncResult ->
        asyncResult.result.sqlResult}

    val errorMessage = MediatorLiveData<String>()





    init {
        errorMessage.addSource(recentBattlesResponse){
            if (it.error != null){
                getErrorMessage(context, it.error)
            }
        }

        awsLambdaFunctionCall(false,
                suspend {
                    _topBattlesLoading.value = true
                    recentBattlesResponse.value = chooseBattleTypeRepository.getRecentBattleList()
                    _topBattlesLoading.value = false
                })
        awsLambdaFunctionCall(false,
                suspend {
                    _recentBattlesLoading.value = true
                    topBattlesResponse.value = topBattlesRepository.getTopBattlesListFromDynamo()
                    _recentBattlesLoading.value = false
                })
    }

    fun doBattleNameSuggestionSearch(battleName : String){
        awsLambdaFunctionCall(false,
                suspend { battleNameSearchResponse.value = chooseBattleTypeRepository.battleTypeSearch(battleName)})
    }

    fun onBattleNameSuggestionClick(view: TextView) {
        battleName.value = view.text.toString()
    }


}
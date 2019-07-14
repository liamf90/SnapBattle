package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.data.BattleNameSearchRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject



/**
 * The ViewModel used in [BattleNameSearchFragment].
 */
class BattleNameSearchViewModel @Inject constructor(private val searchRepository: BattleNameSearchRepository
                                                 ) : ViewModelLaunch() {


    private val searchResultResponse = MutableLiveData<AsyncTaskResult<BattleTypeSuggestionsSearchResponse>>()


    val searchResult : LiveData<List<SuggestionsResponse>> =  Transformations.map(searchResultResponse) { asyncResult ->
        asyncResult.result.sqlResult }

    val errorMessage : LiveData<String?> = Transformations.map(searchResultResponse) { asyncResult ->
        if (asyncResult.error != null){
        getErrorMessage(App.getContext(), asyncResult.error)}
        else null
    }



    fun searchBattle(searchQuery: String) {
        awsLambdaFunctionCall(true,
                suspend {
                    searchResultResponse.value = searchRepository.searchBattleName(searchQuery) })
    }




}
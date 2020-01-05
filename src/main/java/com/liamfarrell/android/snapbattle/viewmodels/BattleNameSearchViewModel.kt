package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.data.BattleNameSearchRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import kotlinx.coroutines.Job
import javax.inject.Inject



/**
 * The ViewModel used in [BattleNameSearchFragment].
 */
class BattleNameSearchViewModel @Inject constructor(private val context: Application, private val searchRepository: BattleNameSearchRepository
                                                 ) : ViewModelBase() {


    private var searchQueryText = ""
    private val searchResultResponse = MutableLiveData<AsyncTaskResult<BattleTypeSuggestionsSearchResponse>>()
    private val _searchResult = MediatorLiveData<List<SuggestionsResponse>>()

    val searchResult : LiveData<List<SuggestionsResponse>> = _searchResult

    private var searchJob: Job? = null

    val errorMessage : LiveData<String?> = Transformations.map(searchResultResponse) { asyncResult ->
        if (asyncResult.error != null){
        getErrorMessage(context, asyncResult.error)}
        else null
    }

    init {
        _searchResult.addSource(searchResultResponse) {
            if (it.error == null) {
                _searchResult.value = it.result.sqlResult
            }
        }
    }

    fun setSearchQueryText(searchQuery: String?) {
        searchQueryText = searchQuery ?: ""
    }



    fun searchBattle(searchQuery: String) {
        if (searchQuery == "") {
            searchJob?.cancel()
            _spinner.value = false
            _searchResult.value = null
        } else {
            searchJob?.cancel()
            if (searchQuery == searchQueryText) {
                searchJob = awsLambdaFunctionCall(true,
                        suspend {
                            if (searchQueryText == searchQuery) {
                                searchResultResponse.value = searchRepository.searchBattleName(searchQuery)
                            }
                        })
            }
        }
    }




}
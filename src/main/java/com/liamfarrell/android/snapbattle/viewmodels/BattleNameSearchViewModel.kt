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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.coroutines.Job
import javax.inject.Inject



/**
 * The ViewModel used in [BattleNameSearchFragment].
 */
class BattleNameSearchViewModel @Inject constructor(private val context: Application, private val searchRepository: BattleNameSearchRepository
                                                 ) : ViewModelLaunch() {


    private var searchQueryText = ""

    private val _searchResult = MutableLiveData<List<SuggestionsResponse>>()
    val searchResult : LiveData<List<SuggestionsResponse>> = _searchResult


    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }



    fun setSearchQueryText(searchQuery: String?) {
        searchQueryText = searchQuery ?: ""
    }



    fun searchBattle(searchQuery: String) {
        if (searchQuery == "") {
            compositeDisposable.clear()
            _spinner.value = false
            _searchResult.value = null
        } else {
            compositeDisposable.clear()
            if (searchQuery == searchQueryText) {
              doServerSearch(searchQuery)
            }
        }
    }

    fun doServerSearch(searchQuery: String){
        compositeDisposable.add(
                searchRepository.searchBattleNameRx(searchQuery)
                        .subscribeOn(io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSuccess { _spinner.value = true }
                        .subscribe(
                                { onSuccessResponse ->
                                    _spinner.value = false
                                    _searchResult.value = onSuccessResponse.sqlResult
                                },
                                {onError : Throwable ->
                                    _spinner.value = false
                                    error.value = onError
                                }
                        ))
    }




}
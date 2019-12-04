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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
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


    private val recentBattlesResponse = MutableLiveData<RecentBattleResponse>()
    val recentBattles : LiveData<List<RecentBattleNamePOJO>> =  Transformations.map(recentBattlesResponse) { asyncResult ->
        asyncResult.sqlResult }

    private val _recentBattlesLoading = MutableLiveData<Boolean>()
    val recentBattlesLoading : LiveData<Boolean> = _recentBattlesLoading


    private val battleNameSearchResponse = MutableLiveData<BattleTypeSuggestionsSearchResponse>()
    val battleNameSearchList : LiveData<List<SuggestionsResponse>> =  Transformations.map(battleNameSearchResponse) { asyncResult ->
        asyncResult.sqlResult}

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }


    init {
        getRecentBattleList()
        getTopBattles()
    }



    private fun getRecentBattleList(){
        compositeDisposable.add(
                chooseBattleTypeRepository.getRecentBattleListRx()
                        .subscribeOn(io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { _recentBattlesLoading.value = true }
                        .subscribe(
                                { onSuccessResponse ->
                                    _recentBattlesLoading.value = false
                                    recentBattlesResponse.value = onSuccessResponse
                                },
                                { onError : Throwable ->
                                    _recentBattlesLoading.value = false
                                    error.value = onError
                                }
                        ))
    }

    private fun getTopBattles(){
        compositeDisposable.add(
                topBattlesRepository.getTopBattlesListFromDynamoRx()
                        .subscribeOn(io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { _topBattlesLoading.value = true }
                        .subscribe(
                                { onSuccessResponse ->
                                    _topBattlesLoading.value = false
                                    topBattlesResponse.value = onSuccessResponse
                                },
                                { onError : Throwable ->
                                    _topBattlesLoading.value = false
                                    error.value = onError
                                }
                        ))
    }





    fun doBattleNameSuggestionSearch(battleName : String){
        compositeDisposable.add(
                chooseBattleTypeRepository.battleTypeSearchRx(battleName)
                        .subscribeOn(io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { onSuccessResponse ->
                                    battleNameSearchResponse.value = onSuccessResponse
                                },
                                { onError : Throwable ->
                                    error.value = onError
                                }
                        ))
    }

    fun onBattleNameSuggestionClick(view: TextView) {
        battleName.value = view.text.toString()
    }


}
package com.liamfarrell.android.snapbattle.viewmodel

import android.app.Application
import android.content.Context
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import com.liamfarrell.android.snapbattle.model.ReportedBattle
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * The ViewModel used in [CommentsReportedFragment].
 */
class BattlesReportedViewModel (val context: Context, val reportingsRepository: ReportingsRepository) : ViewModelLaunch() {
    companion object{
        const val FETCH_AMOUNT = 50
    }


    private val _reportedBattles = MutableLiveData<MutableList<ReportedBattle>>()
    val reportedBattles : LiveData<MutableList<ReportedBattle>> = _reportedBattles

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }





    init {
        loadBattles()
    }

    fun loadBattles(){
        compositeDisposable.add(reportingsRepository.getReportedBattlesRx(FETCH_AMOUNT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            _reportedBattles.value = onSuccessResponse.sqlResult
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    fun deleteBattle(battleId: Int) {

        compositeDisposable.add( reportingsRepository.deleteCommentAdminRx(battleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                           if (onSuccessResponse.affectedRows == 1){
                               _reportedBattles.value?.find { it.battleID == battleId }?.isDeleted = true
                               _reportedBattles.notifyObserver()
                           }

                           else {
                               error.value  = NotAuthorisedToDeleteBattleError()
                            }
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))
    }

    fun ignoreBattle(battleId: Int) {
        compositeDisposable.add( reportingsRepository.ignoreBattleAdminRx(battleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            if (onSuccessResponse.affectedRows == 1){
                                _reportedBattles.value?.find { it.battleID == battleId }?.isBattleIgnored = true
                                _reportedBattles.notifyObserver()
                            }

                            else {
                                error.value  = NotAuthorisedToIgnoreBattleError()
                            }
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))
    }

    fun banChallenger(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int){
        compositeDisposable.add( reportingsRepository.banUserBattleAdminRx(battleId, cognitoIdUserBan, banLengthDays)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            if (onSuccessResponse.affectedRows == 1){
                                _reportedBattles.value?.find { it.battleID == battleId }?.isChallengerBanned = true
                                _reportedBattles.notifyObserver()
                            }

                            else {
                                error.value  = NotAuthorisedToBanUserError()
                            }
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))

    }

    fun banChallenged(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int){
        compositeDisposable.add( reportingsRepository.banUserBattleAdminRx(battleId, cognitoIdUserBan, banLengthDays)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            if (onSuccessResponse.affectedRows == 1){
                                _reportedBattles.value?.find { it.battleID == battleId }?.isChallengedBanned = true
                                _reportedBattles.notifyObserver()
                            }

                            else {
                                error.value  = NotAuthorisedToBanUserError()
                            }
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))
    }

}


class NotAuthorisedToDeleteBattleError : CustomError() {
    override fun getErrorToastMessage(context: Context): String {
        return context.resources.getString(R.string.not_authorised_delete_battle)
    }
}

class NotAuthorisedToIgnoreBattleError : CustomError() {
    override fun getErrorToastMessage(context: Context): String {
        return context.resources.getString(R.string.not_authorised_ignore_battle)
    }
}




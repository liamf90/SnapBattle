package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * The ViewModel used in [ViewOwnBattleFragment].
 */
class ViewOwnBattleViewModel @Inject constructor(private val context: Application, private val usersBattleRepository: UsersBattleRepository) : ViewModelLaunch() {

    private val _battle = MutableLiveData<Battle>()
    val battle : LiveData<Battle> = _battle

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }


    fun getBattle(battleID : Int){
        compositeDisposable.add(usersBattleRepository.getBattleRx(battleID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe{
                    if (_battle.value == null) {
                        _spinner.value = true }
                    }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            _battle.value = onSuccessResponse.sqlResult
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }



}
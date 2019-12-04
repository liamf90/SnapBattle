package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import android.content.Context
import android.view.View
import androidx.lifecycle.*
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.BattlesRepository
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CreateBattleResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.VerifyBattleFragmentDirections
import com.liamfarrell.android.snapbattle.util.BannedError
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.mysqlDateStringToDate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * The ViewModel used in [VerifyBattleFragment].
 */
class VerifyBattleViewModel @Inject constructor(private val context: Application, private val battlesRepository: BattlesRepository) : ViewModelLaunch() {

    private val _battle = MutableLiveData<Battle>()
    val battle : LiveData<Battle> = _battle

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }

    fun setBattle(b: Battle){
        _battle.value = b
    }

    fun createBattle(view: View){
        val b = battle.value
        if (b != null){
            compositeDisposable.add(battlesRepository.createBattleRx(b.challengedFacebookUserId, b.challengedCognitoID, b.battleName, b.rounds, b.voting.votingChoice, b.voting.votingLength)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe{_spinner.value = true}
                    .subscribe(
                            { onSuccessResponse ->
                                _spinner.value = false
                                if (onSuccessResponse.error != null){ error.value = getCreateBattleError(onSuccessResponse)}
                                else {
                                    //successfully created battle, go to next fragment
                                    // set snackbar data to (R.string.battle_request_sent_snackbar_message)
                                    val directions = VerifyBattleFragmentDirections.actionVerifyBattleFragmentToBottomNavigationDrawerFragment(R.string.battle_request_sent_snackbar_message)
                                    view.findNavController().navigate(directions)
                                }
                            },
                            {onError : Throwable ->
                                _spinner.value = false
                                error.value = onError
                            }
                    ))
        }
    }

    private fun getCreateBattleError(createBattleResponse: CreateBattleResponse) : CustomError{
        return when(createBattleResponse.error){
            CreateBattleResponse.battleNameTooLongError -> BattleNameTooLongError()
            CreateBattleResponse.ROUNDS_WRONG_AMOUNT_ERROR -> RoundsWrongAmountError()
            CreateBattleResponse.NOT_BEEN_LONG_ENOUGH_ERROR -> NotBeenLongEnoughError()
            CreateBattleResponse.USER_BANNED_ERROR -> (BannedError(mysqlDateStringToDate(createBattleResponse.timeBanEnds)))
            else -> DefaultError()
        }
    }

}

class BattleNameTooLongError : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.battle_name_too_long_toast) }
}

class RoundsWrongAmountError : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.incorrect_rounds_toast) }
}

class NotBeenLongEnoughError : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.not_long_enough_wait_challenge_opponent_toast) }
}

class DefaultError : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.generic_error_toast) }
}

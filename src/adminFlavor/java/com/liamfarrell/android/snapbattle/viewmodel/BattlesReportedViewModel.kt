package com.liamfarrell.android.snapbattle.viewmodel

import android.content.Context
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.ReportedBattle
import com.liamfarrell.android.snapbattle.model.ReportedComment
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportedBattlesResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportedCommentsResponse
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage

/**
 * The ViewModel used in [CommentsReportedFragment].
 */
class BattlesReportedViewModel(val reportingsRepository: ReportingsRepository) : ViewModelLaunch() {
    companion object{
        const val FETCH_AMOUNT = 50
    }

    private val reportedBattlesResponse = MutableLiveData<AsyncTaskResult<ReportedBattlesResponse>>()

    val reportedBattles : LiveData<List<ReportedBattle>> =  Transformations.map(reportedBattlesResponse) { asyncResult ->
        asyncResult.result.sqlResult }

    val errorMessage : LiveData<String?> = Transformations.map(reportedBattlesResponse) { asyncResult ->
        if (asyncResult.error != null){
            getErrorMessage(App.getContext(), asyncResult.error)}
        else
            null
    }


    init {
        loadBattles()
    }

    fun loadBattles(){
        awsLambdaFunctionCall(true,
                suspend {reportedBattlesResponse.value = reportingsRepository.getReportedBattles(FETCH_AMOUNT)})
    }

    fun deleteBattle(battleId: Int) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = reportingsRepository.deleteCommentAdmin  (battleId)
                    when {
                        response.error != null -> reportedBattlesResponse.value?.error = response.error
                        response.result.affectedRows == 1 -> reportedBattlesResponse.value?.result?.sqlResult?.find { it.battleID == battleId }?.isDeleted = true
                        else -> reportedBattlesResponse.value?.error = NotAuthorisedToDeleteBattleError()
                    }
                    Unit
                })
    }

    fun ignoreBattle(battleId: Int) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = reportingsRepository.ignoreBattleAdmin  (battleId)
                    when {
                        response.error != null -> reportedBattlesResponse.value?.error = response.error
                        response.result.affectedRows == 1 -> reportedBattlesResponse.value?.result?.sqlResult?.find { it.battleId == battleId }?.isBattleIgnored = true
                        else -> reportedBattlesResponse.value?.error = NotAuthorisedToIgnoreBattleError()
                    }
                    Unit
                })
    }

    fun banChallenger(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int){
        awsLambdaFunctionCall(false,
                suspend {
                    val response = reportingsRepository.banUserBattleAdmin(battleId, cognitoIdUserBan, banLengthDays)
                    when {
                        response.error != null -> reportedBattlesResponse.value?.error = response.error
                        response.result.affectedRows == 1 -> reportedBattlesResponse.value?.result?.sqlResult?.find { it.battleId == battleId }?.isChallengerBanned = true
                        else -> reportedBattlesResponse.value?.error = NotAuthorisedToBanUserError()
                    }
                    Unit
                })
    }

    fun banChallenged(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int){
        awsLambdaFunctionCall(false,
                suspend {
                    val response = reportingsRepository.banUserBattleAdmin(battleId, cognitoIdUserBan, banLengthDays)
                    when {
                        response.error != null -> reportedBattlesResponse.value?.error = response.error
                        response.result.affectedRows == 1 -> reportedBattlesResponse.value?.result?.sqlResult?.find { it.battleId == battleId }?.isChallengedBanned = true
                        else -> reportedBattlesResponse.value?.error = NotAuthorisedToBanUserError()
                    }
                    Unit
                })
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




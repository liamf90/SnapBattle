package com.liamfarrell.android.snapbattle.viewmodel

import android.content.Context
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.data.BattleChallengesRepository
import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.ReportedComment
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportedCommentsResponse
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage

/**
 * The ViewModel used in [CommentsReportedFragment].
 */
class CommentsReportedViewModel(val reportingsRepository: ReportingsRepository) : ViewModelLaunch() {
    companion object{
        const val FETCH_AMOUNT = 50
    }

    private val reportedCommentsResponse = MutableLiveData<AsyncTaskResult<ReportedCommentsResponse>>()

    val reportedComments : LiveData<List<ReportedComment>> =  Transformations.map(reportedCommentsResponse) { asyncResult ->
        asyncResult.result.sqlResult }

    val errorMessage : LiveData<String?> = Transformations.map(reportedCommentsResponse) { asyncResult ->
        if (asyncResult.error != null){
            getErrorMessage(App.getContext(), asyncResult.error)}
        else
            null
    }


    init {
        loadComments()
    }

    fun loadComments(){
        awsLambdaFunctionCall(true,
                suspend {reportedCommentsResponse.value = reportingsRepository.getReportedComments(FETCH_AMOUNT)})
    }

    fun deleteComment(commentId: Int) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = reportingsRepository.deleteCommentAdmin  (commentId)
                    when {
                        response.error != null -> reportedCommentsResponse.value?.error = response.error
                        response.result.affectedRows == 1 -> reportedCommentsResponse.value?.result?.sqlResult?.find { it.commentId == commentId }?.isDeleted = true
                        else -> reportedCommentsResponse.value?.error = NotAuthorisedToDeleteCommentError()
                    }
                    Unit
                })
    }

    fun ignoreComment(commentId: Int) {
        awsLambdaFunctionCall(false,
                suspend {
                    val response = reportingsRepository.ignoreBattleAdmin  (commentId)
                    when {
                        response.error != null -> reportedCommentsResponse.value?.error = response.error
                        response.result.affectedRows == 1 -> reportedCommentsResponse.value?.result?.sqlResult?.find { it.commentId == commentId }?.isCommentIgnored = true
                        else -> reportedCommentsResponse.value?.error = NotAuthorisedToIgnoreCommentError()
                    }
                    Unit
                })
    }

    fun banUser(cognitoIdUser: String, commentId: Int, banLengthDays: Int){
        awsLambdaFunctionCall(false,
                suspend {
                    val response = reportingsRepository.banUserCommentAdmin(cognitoIdUser, commentId, banLengthDays)
                    when {
                        response.error != null -> reportedCommentsResponse.value?.error = response.error
                        response.result.affectedRows == 1 -> reportedCommentsResponse.value?.result?.sqlResult?.find { it.commentId == commentId }?.isUserIsBanned = true
                        else -> reportedCommentsResponse.value?.error = NotAuthorisedToBanUserError()
                    }
                    Unit
                })

    }

}


class NotAuthorisedToDeleteCommentError : CustomError() {
    override fun getErrorToastMessage(context: Context): String {
        return context.resources.getString(R.string.not_authorised_delete_comment)
    }
}

class NotAuthorisedToIgnoreCommentError : CustomError() {
    override fun getErrorToastMessage(context: Context): String {
        return context.resources.getString(R.string.not_authorised_ignore_comment)
    }
}

class NotAuthorisedToBanUserError : CustomError() {
    override fun getErrorToastMessage(context: Context): String {
        return context.resources.getString(R.string.not_authorised_ban_user)
    }
}



package com.liamfarrell.android.snapbattle.viewmodel

import android.app.Application
import android.content.Context
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.BattleChallengesRepository
import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.ReportedComment
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportedCommentsResponse
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * The ViewModel used in [CommentsReportedFragment].
 */
class CommentsReportedViewModel @Inject constructor(val context: Context, val reportingsRepository: ReportingsRepository) : ViewModelLaunch() {
    companion object{
        const val FETCH_AMOUNT = 50
    }



    private val _reportedComments = MutableLiveData<MutableList<ReportedComment>>()
    val reportedComments : LiveData<MutableList<ReportedComment>> = _reportedComments


    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }


    init {
        loadComments()
    }

    private fun loadComments(){
        compositeDisposable.add(reportingsRepository.getReportedComments(FETCH_AMOUNT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            _reportedComments.value = onSuccessResponse.sqlResult
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    fun deleteComment(commentId: Int) {
        compositeDisposable.add( reportingsRepository.deleteCommentAdmin(commentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            if (onSuccessResponse.affectedRows == 1){
                                _reportedComments.value?.find { it.commentId == commentId }?.isDeleted = true
                                _reportedComments.notifyObserver()
                            }

                            else {
                                error.value  = NotAuthorisedToDeleteCommentError()
                            }
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))
    }

    fun ignoreComment(commentId: Int) {
        compositeDisposable.add( reportingsRepository.ignoreCommentAdmin(commentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            if (onSuccessResponse.affectedRows == 1){
                                _reportedComments.value?.find { it.commentId == commentId }?.isCommentIgnored = true
                                _reportedComments.notifyObserver()
                            }

                            else {
                                error.value  = NotAuthorisedToIgnoreCommentError()
                            }
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))

    }

    fun banUser(cognitoIdUser: String, commentId: Int, banLengthDays: Int){
        compositeDisposable.add( reportingsRepository.banUserCommentAdmin(cognitoIdUser, commentId, banLengthDays)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            if (onSuccessResponse.affectedRows == 1){
                                _reportedComments.value?.find { it.commentId == commentId }?.isUserIsBanned = true
                                _reportedComments.notifyObserver()
                            } else {
                                error.value  = NotAuthorisedToBanUserError()
                            }
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))

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



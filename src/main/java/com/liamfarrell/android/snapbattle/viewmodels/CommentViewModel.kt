package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import com.liamfarrell.android.snapbattle.data.CommentRepository
import com.liamfarrell.android.snapbattle.model.Comment
import androidx.lifecycle.*
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VerifyUserResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.AddCommentResponse
import com.liamfarrell.android.snapbattle.util.AlreadyFollowingError
import com.liamfarrell.android.snapbattle.util.BannedError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject


/**
 * The ViewModel used in [ViewCommentFragment].
 */
class CommentViewModel @Inject constructor(private val context: Application, private val commentRepository : CommentRepository) : ViewModelLaunch() {

    private val commentsResult = MutableLiveData<AsyncTaskResult<MutableList<Comment>>>()

    val errorMessage : LiveData<String?> = Transformations.map(commentsResult) { result ->
        getErrorMessage(context, result.error)
    }

    val comments : LiveData<MutableList<Comment>>  =  Transformations.map (commentsResult) { result ->
             result.result
    }




    private val _showAddCommentProgressBar = MutableLiveData<Boolean>()
    val showAddCommentProgressBar : LiveData<Boolean> =_showAddCommentProgressBar


    init {
        _showAddCommentProgressBar.value = false
    }

    fun getComments(battleID: Int){
        awsLambdaFunctionCall(true, suspend{commentsResult.value = commentRepository.getComments(battleID)})
    }

    fun deleteComment(commentID: Int) {
        awsLambdaFunctionCall(false,
                suspend {
                        val result = commentRepository.deleteComment(commentID)
                        if (result.error != null) {
                            commentsResult.value?.error = result.error} else {
                        if (result.result.affectedRows == 1) {
                            val deletedComment = commentsResult.value?.result?.find { commentID == commentID }
                            deletedComment?.let {  commentsResult.value?.result?.remove(it) }}}}
        )
    }

    fun addComment(battleID: Int, comment: String, usernamesToTag : List<String>, requestUserFriends: ()->Unit){
        awsLambdaFunctionCall(false,
                suspend {
                        _showAddCommentProgressBar.value = true
                        val asyncResult = commentRepository.addComment(battleID, comment, usernamesToTag)

                        if (asyncResult.error != null && asyncResult.result.error.equals(AddCommentResponse.getUserNotMinimumFriendsError())) {
                            //User not verified with enough facebook friends to post comments.. Verify User
                            //first check if user has user_friends permission allowed
                            if (doesUserHaveUserFriendsPermission()) {
                                verifyUser(battleID, comment, usernamesToTag)
                            } else {
                                commentsResult.value?.error =  AlreadyFollowingError()
                                requestUserFriends() }
                        } else if (asyncResult.error != null && asyncResult.result.error.equals(AddCommentResponse.getUserBannedError())) {
                            //User is banned.
                            commentsResult.value?.error = BannedError(asyncResult.result.timeBanEnds)
                        } else {
                            commentsResult.value?.result?.add(asyncResult.result.sqlResult.get(0))
                            _showAddCommentProgressBar.value = false }
                        })
    }

    fun reportComment(commentID: Int) {
        awsLambdaFunctionCall(true,
                suspend {
                    val asyncResult = commentRepository.reportComment(commentID)
                    if (asyncResult.result.affectedRows == 1) {
                        _snackBarMessage.value = context.getString(R.string.comment_reported_toast)
                    } else {
                        _snackBarMessage.value = context.getString(R.string.comment_already_reported_toast)
                    }
                }
        )
    }

    fun verifyUser(battleID: Int, comment: String, usernameTagsList: List<String>){
        awsLambdaFunctionCall(true,
                suspend {
                    val asyncResult = commentRepository.verifyUser()
                    if (asyncResult.result.getResult().equals(VerifyUserResponse.USER_VERIFIED_RESULT)) {
                        addComment(battleID, comment, usernameTagsList){}
                    }
                    else if (asyncResult.result.getResult().equals(VerifyUserResponse.USER_NOT_VERIFIED_RESULT)) {
                        _snackBarMessage.value = context.getString(R.string.not_enough_facebook_friends_toast)
                    }
                }
        )
    }

    private fun doesUserHaveUserFriendsPermission(): Boolean {
        val declinedPermissions =  AccessToken.getCurrentAccessToken().declinedPermissions
        return !declinedPermissions.contains("user_friends")
    }

}


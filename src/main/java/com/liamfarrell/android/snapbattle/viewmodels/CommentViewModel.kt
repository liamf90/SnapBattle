package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import com.liamfarrell.android.snapbattle.data.CommentRepository
import com.liamfarrell.android.snapbattle.model.Comment
import androidx.lifecycle.*
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VerifyUserResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.AddCommentResponse
import com.liamfarrell.android.snapbattle.util.AlreadyFollowingError
import com.liamfarrell.android.snapbattle.util.BannedError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * The ViewModel used in [ViewCommentFragment].
 */
class CommentViewModel @Inject constructor(private val context: Application, private val commentRepository : CommentRepository,  private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {

    private val commentsResult = MutableLiveData<AsyncTaskResult<MutableList<Comment>>>()

    val errorMessage : LiveData<String?> = Transformations.map(commentsResult) { result ->
        result.error?.let{getErrorMessage(context, it)}
    }

    val comments = MediatorLiveData<MutableList<Comment>>()

    private val profilePicMap = mutableMapOf<String, String>()

    private val _showAddCommentProgressBar = MutableLiveData<Boolean>()
    val showAddCommentProgressBar : LiveData<Boolean> =_showAddCommentProgressBar


    init {
        comments.addSource(commentsResult){
            if (it.error == null){
                    comments.value = it.result
            }
        }
        _showAddCommentProgressBar.value = false
    }

    fun getComments(battleID: Int){
        awsLambdaFunctionCall(true) {
            val response = commentRepository.getComments(battleID)
            if (response.error == null) {
                response.result = getProfilePicSignedUrls(response.result).toMutableList()
            }
            commentsResult.value = response
        }
    }

    fun deleteComment(commentID: Int) {
        awsLambdaFunctionCall(false
        ) {
            val result = commentRepository.deleteComment(commentID)
            if (result.error != null) {
                commentsResult.value = AsyncTaskResult(result.error)
            } else {
                if (result.result.affectedRows == 1) {
                    val deletedComment = commentsResult.value?.result?.find { commentID == commentID }
                    deletedComment?.let {
                        comments.value?.find { it.commentId == commentID}?.isDeleted = true
                        comments.notifyObserver()}}
            }}
    }

    fun addComment(battleID: Int, comment: String, usernamesToTag : List<String>, requestUserFriends: ()->Unit){
        awsLambdaFunctionCall(false
        ) {
            _showAddCommentProgressBar.value = true
            val asyncResult = commentRepository.addComment(battleID, comment, usernamesToTag)

            if (asyncResult.error != null){
                commentsResult.value = AsyncTaskResult(asyncResult.error)
            } else if (asyncResult.result.error != null && asyncResult.result.error == AddCommentResponse.getUserNotMinimumFriendsError()) {
                //User not verified with enough facebook friends to post comments.. Verify User
                //first check if user has user_friends permission allowed
                _showAddCommentProgressBar.value = false
                if (doesUserHaveUserFriendsPermission()) {
                    verifyUser(battleID, comment, usernamesToTag)
                } else {
                    commentsResult.value =  AsyncTaskResult(AlreadyFollowingError())
                    requestUserFriends() }
            } else if (asyncResult.result.error != null && asyncResult.result.error == AddCommentResponse.getUserBannedError()) {
                //User is banned.
                _showAddCommentProgressBar.value = false
                commentsResult.value = AsyncTaskResult(BannedError(asyncResult.result.timeBanEnds))
            } else {
                //no error
                //get profile pic
                asyncResult.result.sqlResult?.let{asyncResult.result.sqlResult = getProfilePicSignedUrls(it)}
                comments.value?.add(asyncResult.result.sqlResult.get(0))
                comments.notifyObserver()
                _showAddCommentProgressBar.value = false }
        }
    }

    fun reportComment(commentID: Int) {
        awsLambdaFunctionCall(true
        ) {
            val asyncResult = commentRepository.reportComment(commentID)
            if (asyncResult.result.affectedRows == 1) {
                _snackBarMessage.value = context.getString(R.string.comment_reported_toast)
            } else {
                _snackBarMessage.value = context.getString(R.string.comment_already_reported_toast)
            }
        }
    }

    fun verifyUser(battleID: Int, comment: String, usernameTagsList: List<String>){
        awsLambdaFunctionCall(true
        ) {
            val asyncResult = commentRepository.verifyUser()
            if (asyncResult.result.getResult().equals(VerifyUserResponse.USER_VERIFIED_RESULT)) {
                addComment(battleID, comment, usernameTagsList){}
            } else if (asyncResult.result.getResult().equals(VerifyUserResponse.USER_NOT_VERIFIED_RESULT)) {
                _snackBarMessage.value = context.getString(R.string.not_enough_facebook_friends_toast)
            }
        }
    }

    private suspend fun getProfilePicSignedUrls(commentList: List<Comment>) : List<Comment>{
        commentList.forEach {
            if (it.commenterProfilePicCount != 0){
                if (profilePicMap.containsKey(it.cognitoIdCommenter)){
                    it.commenterProfilePicSmallSignedUrl = profilePicMap[it.cognitoIdCommenter]
                } else {
                    it.commenterProfilePicSmallSignedUrl = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrl(it.cognitoIdCommenter,  it.commenterProfilePicCount , it.commenterProfilePicSmallSignedUrl )
                    profilePicMap[it.cognitoIdCommenter] = it.commenterProfilePicSmallSignedUrl
                }
            }
        }
        return commentList
    }

    private fun doesUserHaveUserFriendsPermission(): Boolean {
        val declinedPermissions =  AccessToken.getCurrentAccessToken().declinedPermissions
        return !declinedPermissions.contains("user_friends")
    }

}


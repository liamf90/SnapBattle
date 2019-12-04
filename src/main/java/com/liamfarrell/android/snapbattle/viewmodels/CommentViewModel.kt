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
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VerifyUserResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.AddCommentResponse
import com.liamfarrell.android.snapbattle.util.AlreadyFollowingError
import com.liamfarrell.android.snapbattle.util.BannedError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.util.notifyObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * The ViewModel used in [ViewCommentFragment].
 */
class CommentViewModel @Inject constructor(private val context: Application, private val commentRepository : CommentRepository,  private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {

    private val _comments = MutableLiveData<MutableList<Comment>>()
    val comments : LiveData<MutableList<Comment>> = _comments

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }



    private val profilePicMap = mutableMapOf<String, String>()

    private val _showAddCommentProgressBar = MutableLiveData<Boolean>()
    val showAddCommentProgressBar : LiveData<Boolean> =_showAddCommentProgressBar


    init {
        _showAddCommentProgressBar.value = false
    }

    fun getComments(battleID: Int){
        compositeDisposable.add(  commentRepository.getCommentsRx(battleID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .map { getProfilePicSignedUrls(it.sql_result).toMutableList() }
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            _comments.value = onSuccessResponse
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    fun deleteComment(commentID: Int) {
        compositeDisposable.add(   commentRepository.deleteCommentRx(commentID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            if (onSuccessResponse.affectedRows == 1) {
                                val deletedComment = _comments.value?.find { commentID == commentID }
                                deletedComment?.let {
                                    _comments.value?.find { it.commentId == commentID}?.isDeleted = true
                                    _comments.notifyObserver()}}
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))
    }

    fun addComment(battleID: Int, comment: String, usernamesToTag : List<String>, requestUserFriends: ()->Unit) {
        compositeDisposable.add(commentRepository.addCommentRx(battleID, comment, usernamesToTag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { _showAddCommentProgressBar.value = true }
                .subscribe(
                        { onSuccessResponse ->
                            if (onSuccessResponse.error != null && onSuccessResponse.error == AddCommentResponse.getUserNotMinimumFriendsError()) {
                                //User not verified with enough facebook friends to post comments.. Verify User
                                //first check if user has user_friends permission allowed
                                _showAddCommentProgressBar.value = false
                                if (doesUserHaveUserFriendsPermission()) {
                                    verifyUser(battleID, comment, usernamesToTag)
                                } else {
                                    error.value =  AlreadyFollowingError()
                                    requestUserFriends() }
                            } else if (onSuccessResponse.error != null && onSuccessResponse.error == AddCommentResponse.getUserBannedError()) {
                                //User is banned.
                                _showAddCommentProgressBar.value = false
                                error.value = BannedError(onSuccessResponse.timeBanEnds)
                            } else {
                                //no error
                                //get profile pic
                                onSuccessResponse.sqlResult?.let{onSuccessResponse.sqlResult = getProfilePicSignedUrls(it)}
                                _comments.value?.add(onSuccessResponse.sqlResult.get(0))
                                _comments.notifyObserver()
                                _showAddCommentProgressBar.value = false }
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))
    }



    fun reportComment(commentID: Int) {
        compositeDisposable.add(commentRepository.reportCommentRx(commentID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            if (onSuccessResponse.affectedRows == 1) {
                                _snackBarMessage.value = context.getString(R.string.comment_reported_toast)
                            } else {
                                _snackBarMessage.value = context.getString(R.string.comment_already_reported_toast)
                            }
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    fun verifyUser(battleID: Int, comment: String, usernameTagsList: List<String>){
        compositeDisposable.add(  commentRepository.verifyUserRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            if (onSuccessResponse.result.equals(VerifyUserResponse.USER_VERIFIED_RESULT)) {
                                addComment(battleID, comment, usernameTagsList){}
                            } else if (onSuccessResponse.result.equals(VerifyUserResponse.USER_NOT_VERIFIED_RESULT)) {
                                _snackBarMessage.value = context.getString(R.string.not_enough_facebook_friends_toast)
                            }
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    private fun getProfilePicSignedUrls(commentList: List<Comment>) : List<Comment>{
        commentList.forEach {
            if (it.commenterProfilePicCount != 0){
                if (profilePicMap.containsKey(it.cognitoIdCommenter)){
                    it.commenterProfilePicSmallSignedUrl = profilePicMap[it.cognitoIdCommenter]
                } else {
                    it.commenterProfilePicSmallSignedUrl = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrlRx(it.cognitoIdCommenter,  it.commenterProfilePicCount , it.commenterProfilePicSmallSignedUrl )
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


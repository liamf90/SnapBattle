package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.*
import javax.inject.Inject
import javax.inject.Singleton
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.*
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction


@Singleton
class CommentRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {




    suspend fun getComments(battleID: Int) : AsyncTaskResult<GetCommentsResponse> {
        return executeRestApiFunction(snapBattleApiService.getComments(battleID))
    }

    suspend fun addComment(battleID: Int, comment: String, usernamesToTag : List<String>) : AsyncTaskResult<AddCommentResponse> {
        val commentRequest = AddCommentRequest()
        commentRequest.comment = comment
        commentRequest.usernamesToTag = usernamesToTag
        return executeRestApiFunction(snapBattleApiService.addComment (battleID, commentRequest))
    }

    suspend fun reportComment(commentID: Int, battleID: Int) : AsyncTaskResult<ReportCommentResponse> {
        val reportCommentRequest = ReportCommentRequest()
        reportCommentRequest.setCommentID(commentID);
        return executeRestApiFunction(snapBattleApiService.reportComment (commentID, battleID))
    }

    suspend fun verifyUser() : AsyncTaskResult<VerifyUserResponse> {
        return executeRestApiFunction(snapBattleApiService.verifyUser (VerifyUserRequest().apply {accessToken = AccessToken.getCurrentAccessToken().token }))
    }

    suspend fun deleteComment(commentID: Int, battleID: Int) :   AsyncTaskResult<DeleteCommentResponse> {
        val deleteRequest = DeleteCommentRequest()
        deleteRequest.commentID = commentID
        return executeRestApiFunction(snapBattleApiService.deleteComment (commentID, battleID))
    }




}
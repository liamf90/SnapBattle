package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.*
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.security.Key
import javax.inject.Inject
import javax.inject.Singleton
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.model.Comment
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.*


@Singleton
class CommentRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {




    suspend fun getComments(battleID: Int) : AsyncTaskResult<MutableList<Comment>> {
        val commentRequest = GetCommentsRequest()
        commentRequest.battleID = battleID
        return executeAWSFunction {lambdaFunctionsInterface.GetComments(commentRequest).sql_result}
    }

    suspend fun addComment(battleID: Int, comment: String, usernamesToTag : List<String>) : AsyncTaskResult<AddCommentResponse> {
        val commentRequest = AddCommentRequest()
        commentRequest.battleID = battleID
        commentRequest.comment = comment
        commentRequest.usernamesToTag = usernamesToTag
        return executeAWSFunction {lambdaFunctionsInterface.AddComment(commentRequest)}
    }

    suspend fun reportComment(commentID: Int) : AsyncTaskResult<ReportCommentResponse> {
        val reportCommentRequest = ReportCommentRequest()
        reportCommentRequest.setCommentID(commentID);
        return executeAWSFunction {lambdaFunctionsInterface.ReportComment(reportCommentRequest)}
    }

    suspend fun verifyUser() : AsyncTaskResult<VerifyUserResponse> {
        val request = VerifyUserRequest()
        request.setAccessToken(AccessToken.getCurrentAccessToken().token)
        return executeAWSFunction {lambdaFunctionsInterface.VerifyUser(request)}
    }

    suspend fun deleteComment(commentID: Int) :   AsyncTaskResult<DeleteCommentResponse> {
        val deleteRequest = DeleteCommentRequest()
        deleteRequest.commentID = commentID
        return executeAWSFunction {lambdaFunctionsInterface.DeleteComment(deleteRequest)}
    }




}
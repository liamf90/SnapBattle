package com.liamfarrell.android.snapbattle.data

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.DeleteCommentRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetCommentsRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DeleteCommentResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetCommentsResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.security.Key
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun getComments(battleID: Int) :  AsyncTaskResult<GetCommentsResponse> {
        val commentRequest = GetCommentsRequest()
        commentRequest.battleID = battleID
        return executeAWSFunction { lambdaFunctionsInterface.GetComments(commentRequest) }
    }

    /*
      suspend fun getComments(battleID: Int) :  AsyncTaskResult<GetCommentsResponse> {
        val commentRequest = GetCommentsRequest()
        commentRequest.battleID = battleID
          return withContext(IO){
              try {
                   AsyncTaskResult(lambdaFunctionsInterface.GetComments(commentRequest))
              }
              catch (lfe : LambdaFunctionException){
                  AsyncTaskResult<GetCommentsResponse>(lfe)
              }
              catch ( ase: AmazonServiceException){
                  AsyncTaskResult<GetCommentsResponse>(ase)
              }
              catch (ace : AmazonClientException){
                  AsyncTaskResult<GetCommentsResponse>(ace)
              }
         }
    }
    */

    /*
    suspend fun deleteComment(commentID: Int) :  AsyncTaskResult<DeleteCommentResponse> {
        val deleteRequest = DeleteCommentRequest()
        deleteRequest.commentID = commentID
        return withContext(IO){
            try {
                AsyncTaskResult(lambdaFunctionsInterface.DeleteComment(deleteRequest))
            }
            catch (lfe : LambdaFunctionException){
                AsyncTaskResult<DeleteCommentResponse>(lfe)
            }
            catch ( ase: AmazonServiceException){
                AsyncTaskResult<DeleteCommentResponse>(ase)
            }
            catch (ace : AmazonClientException){
                AsyncTaskResult<DeleteCommentResponse>(ace)
            }
        }
    }
    */
    suspend fun deleteComment(commentID: Int) :  AsyncTaskResult<DeleteCommentResponse> {
        val deleteRequest = DeleteCommentRequest()
        deleteRequest.commentID = commentID
        return executeAWSFunction { lambdaFunctionsInterface.DeleteComment(deleteRequest) }
    }


//
//    companion object {
//        // For Singleton instantiation
//        @Volatile private var instance: CommentRepository? = null
//
//        fun getInstance(lambdaFunctionsInterface: LambdaFunctionsInterface) =
//                instance ?: synchronized(this) {
//                    instance ?: CommentRepository(lambdaFunctionsInterface).also { instance = it }
//                }
//    }


}
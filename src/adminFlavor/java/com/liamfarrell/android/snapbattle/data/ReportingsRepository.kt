package com.liamfarrell.android.snapbattle.data

import android.view.View
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Voting
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.*
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.*
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import javax.inject.Inject

class ReportingsRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {
    suspend fun getReportedBattles(loadAmount: Int) : AsyncTaskResult<ReportedBattlesResponse> {
        val request = ReportedBattlesRequest()
        request.fetchLimit = loadAmount
        return executeAWSFunction {lambdaFunctionsInterface.GetReportedBattles(request)}
    }


    suspend fun deleteBattleAdmin(battleId: Int) : AsyncTaskResult<DeleteBattleResponse> {
        val request = DeleteBattleRequest()
        request.battleID = battleId
        return executeAWSFunction {lambdaFunctionsInterface.DeleteBattleAdmin(request)}
    }

    suspend fun ignoreBattleAdmin(battleId: Int) : AsyncTaskResult<IgnoreBattleResponse> {
        val request = IgnoreBattleRequest()
        request.battleID = battleId
        return executeAWSFunction {lambdaFunctionsInterface.IgnoreBattleAdmin(request)}
    }

    suspend fun banUserBattleAdmin(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int) : AsyncTaskResult<BanUserResponse> {
            val request = BanUserRequest()
            request.battleIDReason = battleId
            request.cognitoIDUser = cognitoIdUserBan
            request.banLengthDays = banLengthDays
        return executeAWSFunction {lambdaFunctionsInterface.BanUserAdmin(request)}
    }

    suspend fun getReportedComments(loadAmount: Int) : AsyncTaskResult<ReportedCommentsResponse> {
        val request = ReportedCommentsRequest()
        request.fetchLimit = loadAmount
        return executeAWSFunction {lambdaFunctionsInterface.GetReportedComments(request)}
    }


    suspend fun deleteCommentAdmin(commentId: Int) : AsyncTaskResult<DeleteCommentResponse> {
        val commentRequest = DeleteCommentRequest()
        commentRequest.commentID = commentId
        return executeAWSFunction {lambdaFunctionsInterface.DeleteCommentAdmin(commentRequest)}
    }

    suspend fun ignoreCommentAdmin(commentId: Int) : AsyncTaskResult<IgnoreCommentResponse> {
        val commentRequest = IgnoreCommentRequest()
        commentRequest.commentID = commentId
        return executeAWSFunction {lambdaFunctionsInterface.IgnoreCommentAdmin(commentRequest)}
    }

    suspend fun banUserCommentAdmin(cognitoIdUser: String, commentId: Int, banLengthDays: Int) : AsyncTaskResult<BanUserResponse> {
        val request = BanUserRequest()
        request.cognitoIDUser = cognitoIdUser
        request.commentIDReason = commentId
        request.banLengthDays = banLengthDays
        return executeAWSFunction {lambdaFunctionsInterface.BanUserAdmin(request)}
    }


}

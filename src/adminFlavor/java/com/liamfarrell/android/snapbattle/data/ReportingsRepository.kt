package com.liamfarrell.android.snapbattle.data

import android.view.View
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Voting
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.*
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.*
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import io.reactivex.Single
import javax.inject.Inject

class ReportingsRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    fun getReportedBattlesRx(loadAmount: Int) : Single<ReportedBattlesResponse> {
        val request = ReportedBattlesRequest()
        request.fetchLimit = loadAmount
        return Single.fromCallable {lambdaFunctionsInterface.GetReportedBattles(request)}
    }


    fun ignoreBattleAdminRx(battleId: Int) : Single<IgnoreBattleResponse> {
        val request = IgnoreBattleRequest()
        request.battleID = battleId
        return Single.fromCallable {lambdaFunctionsInterface.IgnoreBattleAdmin(request)}
    }

    fun ignoreCommentAdmin(commentId: Int) : Single<IgnoreCommentResponse> {
        val request = IgnoreCommentRequest()
        request.commentID = commentId
        return Single.fromCallable {lambdaFunctionsInterface.IgnoreCommentAdmin(request)}
    }


    fun banUserBattleAdminRx(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int) : Single<BanUserResponse> {
        val request = BanUserRequest()
        request.battleIDReason = battleId
        request.cognitoIDUser = cognitoIdUserBan
        request.banLengthDays = banLengthDays
        return Single.fromCallable {lambdaFunctionsInterface.BanUserAdmin(request)}
    }

     fun getReportedComments(loadAmount: Int) : Single<ReportedCommentsResponse> {
        val request = ReportedCommentsRequest()
        request.fetchLimit = loadAmount
        return Single.fromCallable {lambdaFunctionsInterface.GetReportedComments(request)}
    }


     fun deleteCommentAdmin(commentId: Int) : Single<DeleteCommentResponse> {
        val commentRequest = DeleteCommentRequest()
        commentRequest.commentID = commentId
        return Single.fromCallable {lambdaFunctionsInterface.DeleteCommentAdmin(commentRequest)}
    }

    fun deleteCommentAdminRx(commentId: Int) : Single<DeleteCommentResponse> {
        val commentRequest = DeleteCommentRequest()
        commentRequest.commentID = commentId
        return Single.fromCallable {lambdaFunctionsInterface.DeleteCommentAdmin(commentRequest)}
    }


     fun banUserCommentAdmin(cognitoIdUser: String, commentId: Int, banLengthDays: Int) : Single<BanUserResponse> {
        val request = BanUserRequest()
        request.cognitoIDUser = cognitoIdUser
        request.commentIDReason = commentId
        request.banLengthDays = banLengthDays
        return Single.fromCallable {lambdaFunctionsInterface.BanUserAdmin(request)}
    }


}

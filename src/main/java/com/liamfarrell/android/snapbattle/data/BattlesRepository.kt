package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetFriendsBattlesRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetFriendsBattlesRequestOld
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetFriendsBattlesResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattlesRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {


    suspend fun getFriendsBattles(battleIDList: List<Int>, lastTimeBattlesUpdated : Date) : AsyncTaskResult<GetFriendsBattlesResponse> {
        val request = GetFriendsBattlesRequest()
        request.battleIDList = battleIDList
        request.lastUpdatedDate = lastTimeBattlesUpdated
        return executeAWSFunction { lambdaFunctionsInterface.GetFriendsBattles(request)}
    }


    suspend fun getFriendsBattles(battleIDList: List<Int>) : AsyncTaskResult<GetFriendsBattlesResponse> {
        val request = GetFriendsBattlesRequest()
        request.battleIDList = battleIDList
        return executeAWSFunction { lambdaFunctionsInterface.GetFriendsBattles(request)}
    }


}
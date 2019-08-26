package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CompletedBattlesRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersBattlesRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CompletedBattlesResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


class CompletedBattlesRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun getCompletedBattles() : AsyncTaskResult<CompletedBattlesResponse>{
        val battlesRequest = CompletedBattlesRequest()
        return executeAWSFunction {lambdaFunctionsInterface.getCompletedBattles(battlesRequest)}
    }

}

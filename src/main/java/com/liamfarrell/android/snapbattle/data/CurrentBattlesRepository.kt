package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CompletedBattlesRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CurrentBattlesRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CompletedBattlesResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class CurrentBattlesRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun getCurrentBattles() : AsyncTaskResult<CurrentBattleResponse> {
        val battlesRequest = CurrentBattlesRequest()
        return executeAWSFunction {lambdaFunctionsInterface.getCurrentBattle(battlesRequest)}
    }

    fun getCurrentBattlesRxJava() : Single<CurrentBattleResponse> {
        val battlesRequest = CurrentBattlesRequest()
        return  Single.fromCallable{lambdaFunctionsInterface.getCurrentBattle(battlesRequest)}
    }
}

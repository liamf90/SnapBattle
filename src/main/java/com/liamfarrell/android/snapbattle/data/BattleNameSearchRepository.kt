package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleTypeSuggestionsSearchRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@OpenForTesting
class BattleNameSearchRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun searchBattleName(searchQuery : String) : AsyncTaskResult<BattleTypeSuggestionsSearchResponse> {
        val request = BattleTypeSuggestionsSearchRequest()
        request.searchName = searchQuery
        return executeAWSFunction {lambdaFunctionsInterface.BattleTypeSuggestionsSearch(request)}
    }
}

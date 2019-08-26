package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleTypeSuggestionsSearchRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetProfileResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.RecentBattleResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import javax.inject.Inject

class ChooseBattleTypeRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun battleTypeSearch(searchName: String): AsyncTaskResult<BattleTypeSuggestionsSearchResponse> {
        val request = BattleTypeSuggestionsSearchRequest()
        request.searchName = searchName
        return executeAWSFunction { lambdaFunctionsInterface.BattleTypeSuggestionsSearch(request) }
    }

    suspend fun getRecentBattleList(): AsyncTaskResult<RecentBattleResponse> {
        return executeAWSFunction { lambdaFunctionsInterface.GetRecentBattleNames() }
    }
}
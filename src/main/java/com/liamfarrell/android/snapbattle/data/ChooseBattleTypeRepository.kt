package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.RecentBattleResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject

class ChooseBattleTypeRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {

    suspend fun battleTypeSearch(searchName: String): AsyncTaskResult<BattleTypeSuggestionsSearchResponse> {
        return executeRestApiFunction(snapBattleApiService.battleTypeSuggestionsSearch(searchName))

    }

    suspend fun getRecentBattleList(): AsyncTaskResult<RecentBattleResponse> {
        return executeRestApiFunction(snapBattleApiService.getRecentBattleNames())
    }
}
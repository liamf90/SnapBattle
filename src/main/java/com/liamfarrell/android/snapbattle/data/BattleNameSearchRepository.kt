package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@OpenForTesting
class BattleNameSearchRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {

    suspend fun searchBattleName(searchQuery : String) : AsyncTaskResult<BattleTypeSuggestionsSearchResponse> {
        return executeRestApiFunction(snapBattleApiService.battleTypeSuggestionsSearch(searchQuery))
    }
}

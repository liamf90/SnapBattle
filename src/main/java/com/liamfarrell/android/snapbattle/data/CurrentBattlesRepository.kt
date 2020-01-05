package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class CurrentBattlesRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {

    suspend fun getCurrentBattles() : AsyncTaskResult<CurrentBattleResponse> {
        return executeRestApiFunction(snapBattleApiService.getCurrentBattles())
    }
}

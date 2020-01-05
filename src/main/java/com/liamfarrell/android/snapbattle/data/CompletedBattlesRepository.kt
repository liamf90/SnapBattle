package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CompletedBattlesResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject


class CompletedBattlesRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {

    suspend fun getCompletedBattles() : AsyncTaskResult<CompletedBattlesResponse>{
        return executeRestApiFunction(snapBattleApiService.getCompletedBattles (null, null))
    }

}

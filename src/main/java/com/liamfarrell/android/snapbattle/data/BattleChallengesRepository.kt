package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateBattleAcceptedRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject

class BattleChallengesRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {

    suspend fun getBattleChallenges() : AsyncTaskResult<GetChallengesResponse> {
        return executeRestApiFunction(snapBattleApiService.getChallenges())
    }

    suspend fun updateBattleAccepted(accepted: Boolean, battleId: Int) : AsyncTaskResult<DefaultResponse> {
        return executeRestApiFunction(snapBattleApiService.updateBattleAccepted(battleId, UpdateBattleAcceptedRequest().apply{isBattleAccepted = accepted} ))
    }





}

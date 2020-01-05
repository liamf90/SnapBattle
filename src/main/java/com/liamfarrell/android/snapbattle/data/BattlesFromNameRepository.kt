package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetBattlesByNameResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattlesFromNameRepository @Inject constructor
( private val snapBattleApiService: SnapBattleApiService) {

    suspend fun getBattlesFromName(battleName: String, afterBattleID: Int = -1) : AsyncTaskResult<GetBattlesByNameResponse> {
        return executeRestApiFunction(snapBattleApiService.getBattlesByName(battleName, BATTLES_PER_FETCH,afterBattleID ))
    }


    companion object{
        val BATTLES_PER_FETCH : Int = 20
    }

}

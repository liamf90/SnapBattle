package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetBattlesByNameRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetBattlesByNameResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattlesFromNameRepository @Inject constructor
( private val lambdaFunctionsInterface: LambdaFunctionsInterface) {




    suspend fun getBattlesFromName(battleName: String, afterBattleID: Int = -1) : AsyncTaskResult<GetBattlesByNameResponse> {
        val battlesRequest = GetBattlesByNameRequest()
        battlesRequest.battleName = battleName
        battlesRequest.fetchLimit = BATTLES_PER_FETCH
        battlesRequest.afterBattleID = afterBattleID
        return executeAWSFunction {lambdaFunctionsInterface.GetBattlesByName(battlesRequest)}
    }



    companion object{
        val BATTLES_PER_FETCH : Int = 20
    }

}

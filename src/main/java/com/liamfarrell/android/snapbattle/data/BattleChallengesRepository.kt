package com.liamfarrell.android.snapbattle.data

import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateBattleAcceptedRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetChallengesResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class BattleChallengesRepository @Inject constructor
(private val lambdaFunctionsInterface: LambdaFunctionsInterface) {

    suspend fun getBattleChallenges() : AsyncTaskResult<GetChallengesResponse> {
        return executeAWSFunction {lambdaFunctionsInterface.getBattleChallenges()}
    }

    suspend fun updateBattleAccepted(accepted: Boolean, battleId: Int) : AsyncTaskResult<DefaultResponse> {
        val request = UpdateBattleAcceptedRequest()
        request.isBattleAccepted = accepted
        request.battleID = battleId
        return executeAWSFunction {lambdaFunctionsInterface.UpdateBattleAccepted(request)}
    }

    fun updateBattleAcceptedRxJava(accepted: Boolean, battleId: Int) : Completable {
        val request = UpdateBattleAcceptedRequest()
        request.isBattleAccepted = accepted
        request.battleID = battleId
        return Completable.fromCallable {lambdaFunctionsInterface.UpdateBattleAccepted(request)}
    }

    fun getBattleChallengesRxJava() : Single<GetChallengesResponse> {
        return  Single.fromCallable{lambdaFunctionsInterface.battleChallenges}
    }




}

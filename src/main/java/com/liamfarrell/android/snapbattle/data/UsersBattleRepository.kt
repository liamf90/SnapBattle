package com.liamfarrell.android.snapbattle.data

import android.content.Context
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.VideoSubmittedRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VideoSubmittedResponse
import com.liamfarrell.android.snapbattle.util.executeAWSFunction
import com.liamfarrell.android.snapbattle.util.uploadVideoJob
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UsersBattleRepository @Inject constructor
( val lambdaFunctionsInterface: LambdaFunctionsInterface) {


    suspend fun getBattle(battleID: Int) :  AsyncTaskResult<Battle> {
        val request = BattleRequest()
        request.battleID = battleID.toString()
        return executeAWSFunction { lambdaFunctionsInterface.getBattleFunction(request).sqlResult}
    }


    suspend fun videoSubmitted(videoID: Int, battleID: Int, videoRotationLock: String?) : AsyncTaskResult<VideoSubmittedResponse> {
        val request = VideoSubmittedRequest()
        request.battleID = battleID
        request.videoID = videoID
        videoRotationLock?.let { request.videoRotationLock = it }
        return executeAWSFunction { lambdaFunctionsInterface.VideoSubmitted(request)}
    }

    suspend fun uploadVideo (context: Context, battle : Battle, fileName : String, cognitoIDOpponent : String, videoID : Int, videoRotationLock: String?) : AsyncTaskResult<VideoSubmittedResponse>{
        val asyncResult = uploadVideoJob(context, fileName, cognitoIDOpponent)
        if (asyncResult.error != null) return AsyncTaskResult(asyncResult.error)
        return videoSubmitted(videoID, battle.battleID, videoRotationLock)
    }


}
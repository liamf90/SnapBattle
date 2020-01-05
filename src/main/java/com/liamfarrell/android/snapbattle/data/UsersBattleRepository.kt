package com.liamfarrell.android.snapbattle.data

import android.content.Context
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.BattleRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.VideoSubmittedRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseBattle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.VideoSubmittedResponse
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import com.liamfarrell.android.snapbattle.util.uploadVideoJob
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UsersBattleRepository @Inject constructor
( private val snapBattleApiService: SnapBattleApiService) {


    suspend fun getBattle(battleID: Int) :  AsyncTaskResult<ResponseBattle> {
        val request = BattleRequest()
        request.battleID = battleID.toString()
        return executeRestApiFunction(snapBattleApiService.getBattle(battleID))
    }


    suspend fun videoSubmitted(videoID: Int, battleID: Int, videoRotationLock: String?) : AsyncTaskResult<VideoSubmittedResponse> {
        val request = VideoSubmittedRequest()
        request.videoID = videoID
        videoRotationLock?.let { request.videoRotationLock = it }
        return executeRestApiFunction(snapBattleApiService.videoSubmitted(battleID, request))
    }

    suspend fun uploadVideo (context: Context, battle : Battle, fileName : String, cognitoIDOpponent : String, videoID : Int, videoRotationLock: String?) : AsyncTaskResult<VideoSubmittedResponse>{
        val asyncResult = uploadVideoJob(context, fileName, cognitoIDOpponent)
        if (asyncResult.error != null) return AsyncTaskResult(asyncResult.error)
        return videoSubmitted(videoID, battle.battleID, videoRotationLock)
    }


}
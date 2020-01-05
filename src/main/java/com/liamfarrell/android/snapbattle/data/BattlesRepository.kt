package com.liamfarrell.android.snapbattle.data

import com.google.gson.Gson
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.CreateBattleRequest
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CreateBattleResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetFriendsBattlesResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.util.executeRestApiFunction
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class BattlesRepository @Inject constructor
(private val snapBattleApiService: SnapBattleApiService) {


    suspend fun getFriendsBattles(battleIDList: List<Int>, lastTimeBattlesUpdated : Date) : AsyncTaskResult<GetFriendsBattlesResponse> {
        val json = Gson().toJson(battleIDList)
        return executeRestApiFunction(snapBattleApiService.getOtherUsersBattles( json.toString(),lastTimeBattlesUpdated ))
    }


    suspend fun getFriendsBattles(battleIDList: List<Int>) : AsyncTaskResult<GetFriendsBattlesResponse> {

        val json = Gson().toJson(battleIDList)
        Timber.i("JSON: $json")
        return executeRestApiFunction(snapBattleApiService.getOtherUsersBattles(json.toString(), null))
    }

    suspend fun createBattle(opponentFacebookId: String?, opponentCognitoId : String?, battleName: String, numberOfRounds: Int, chosenVotingType : ChooseVotingFragment.VotingChoice, votingLength: com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment.VotingLength?) : AsyncTaskResult<
            CreateBattleResponse> {
        val createBattleRequest = CreateBattleRequest()
        if (opponentFacebookId != null) {
            createBattleRequest.challengedFacebookID = opponentFacebookId
        } else if (opponentCognitoId != null) {
            createBattleRequest.challengedCognitoID = opponentCognitoId
        }
        createBattleRequest.votingChoice = chosenVotingType.name
        if (chosenVotingType !== ChooseVotingFragment.VotingChoice.NONE && votingLength != null) {
            createBattleRequest.votingLength = votingLength.name
        }
        createBattleRequest.numberOfRounds = numberOfRounds
        createBattleRequest.battleName = battleName
        return executeRestApiFunction(snapBattleApiService.createBattle(createBattleRequest))
    }


}
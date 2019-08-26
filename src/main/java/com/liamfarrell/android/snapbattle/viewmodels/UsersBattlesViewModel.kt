package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.UsersBattlesRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject

/**
 * The ViewModel used in [UsersBattlesFragment].
 */
class UsersBattlesViewModel @Inject constructor(private val context: Application, private val usersBattlesRepository: UsersBattlesRepository) : ViewModelLaunch() {

    private lateinit var cognitoId : String

    private val battlesResult = MutableLiveData<AsyncTaskResult<GetUsersBattlesResponse>>()

    val errorMessage : LiveData<String?> = Transformations.map(battlesResult) { result ->
        getErrorMessage(context, result.error)
    }

    val battles : LiveData<List<Battle>> =  Transformations.map (battlesResult) { result ->
        result.result.user_battles.filter { !it.isDeleted }
    }

    val user : LiveData<User> =  Transformations.map (battlesResult) { result ->
        result.result.user_profile
    }


    fun setCognitoId(cognitoID: String){
        cognitoId = cognitoID
        awsLambdaFunctionCall(true,
                suspend {battlesResult.value = usersBattlesRepository.getUsersBattles(cognitoID)})
    }

    fun followUser(){
        awsLambdaFunctionCall(false,
                suspend {
                    val response = usersBattlesRepository.followUser(cognitoId)
                    if (response.error == null){
                        battlesResult.value?.result?.user_profile?.isFollowing = true
                    } else{
                        battlesResult.value?.error = response.error
                    }})
    }

    fun unfollowUser(){
        awsLambdaFunctionCall(false,
                suspend {
                    val response = usersBattlesRepository.unfollowUser(cognitoId)
                    if (response.error == null){
                        battlesResult.value?.result?.user_profile?.isFollowing = false
                    } else{
                        battlesResult.value?.error = response.error
                    }
                }
        )
    }

}
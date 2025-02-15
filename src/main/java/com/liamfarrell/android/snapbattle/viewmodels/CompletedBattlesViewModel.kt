package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.data.CompletedBattlesRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CompletedBattlesResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject


/**
 * The ViewModel used in [BattleCompletedListFragment].
 */
class CompletedBattlesViewModel @Inject constructor(private val context: Application, private val completedBattlesRepository: CompletedBattlesRepository, val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelBase() {

    private val battlesResult = MutableLiveData<AsyncTaskResult<CompletedBattlesResponse>>()

    val errorMessage = MediatorLiveData<String>()
    val battles = MediatorLiveData<List<Battle>>()


    init {
        errorMessage.addSource(battlesResult) { result ->
            result?.error?.let { errorMessage.value = getErrorMessage(context, result.error) }
        }

        battles.addSource(battlesResult) { result ->
            result?.let { battles.value = result.result.sqlResult }
        }

        awsLambdaFunctionCall(true,
                suspend {
                    val response = completedBattlesRepository.getCompletedBattles()
                    if (response.error == null) {
                        response.result.sqlResult = getThumbnailSignedUrls(response.result.sqlResult)
                    }
                    battlesResult.value = response
                })
    }

    private suspend fun getThumbnailSignedUrls(battleList: List<Battle>) : List<Battle>{
        battleList.forEach {
            it.signedThumbnailUrl = thumbnailSignedUrlCacheRepository.getThumbnailSignedUrl(it)
        }
        return battleList
    }

}
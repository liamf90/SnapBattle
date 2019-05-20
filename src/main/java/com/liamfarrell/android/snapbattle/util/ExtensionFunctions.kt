package com.liamfarrell.android.snapbattle.util

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <R> executeAWSFunction(awsFunctionCall : () -> R ) : AsyncTaskResult<R> {
    return withContext(Dispatchers.IO) {
        try{
            AsyncTaskResult<R>(awsFunctionCall())
        }
        catch (lfe : LambdaFunctionException){
            AsyncTaskResult<R>(lfe)
        }
        catch ( ase: AmazonServiceException){
            AsyncTaskResult<R>(ase)
        }
        catch (ace : AmazonClientException){
            AsyncTaskResult<R>(ace)
        }
    }
}


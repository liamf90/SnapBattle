package com.liamfarrell.android.snapbattle.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.google.gson.JsonParser
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

suspend fun <R> executeAWSFunction(awsFunctionCall : () -> R ) : AsyncTaskResult<R> {
    return withContext(Dispatchers.IO) {
        try{
            AsyncTaskResult<R>(awsFunctionCall())
        }
        catch (lfe : LambdaFunctionException){
            Timber.i(lfe)
            AsyncTaskResult<R>(lfe)
        }
        catch ( ase: AmazonServiceException){
            Timber.i(ase)
            AsyncTaskResult<R>(ase)
        }
        catch (ace : AmazonClientException){
            Timber.i(ace)
            AsyncTaskResult<R>(ace)
        }
    }
}

suspend fun <R> executeAWSFunction2(awsFunctionCall : () -> R ) : AsyncTaskResult<R> {
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




fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}



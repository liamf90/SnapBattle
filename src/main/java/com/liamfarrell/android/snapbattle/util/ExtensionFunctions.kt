package com.liamfarrell.android.snapbattle.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import timber.log.Timber
import java.net.UnknownHostException

suspend fun <R> executeRestApiFunction(sbServiceCall: Call<R>) : AsyncTaskResult<R> {
    return withContext(Dispatchers.IO) {

        try{
            val response = sbServiceCall.execute()
            if (response.isSuccessful){
                return@withContext AsyncTaskResult<R>(response.body())
            } else if (response.code() == 500) {
                return@withContext AsyncTaskResult<R>(LambdaServerError())
            }
            else {
                return@withContext AsyncTaskResult<R>(LambdaServerError())
            }
        }
        catch (uhe : UnknownHostException){
            Timber.i(uhe)
            return@withContext AsyncTaskResult<R>(uhe)
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



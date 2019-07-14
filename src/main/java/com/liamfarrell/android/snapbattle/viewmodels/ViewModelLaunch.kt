package com.liamfarrell.android.snapbattle.viewmodels

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.switchMap
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.facebook.internal.Mutable
import com.google.gson.JsonParser
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import com.liamfarrell.android.snapbattle.util.HandleLambdaError.ALREADY_FOLLOWING_ERROR
import kotlinx.coroutines.*
import java.lang.Exception


open class ViewModelLaunch : ViewModel() {



    protected val _spinner = MutableLiveData<Boolean>()

    val spinner : LiveData<Boolean> = _spinner


    protected val _snackBarMessage = MutableLiveData<String>()
    val snackBarMessage : LiveData<String> = _snackBarMessage



    /**
     * Helper function to call a data load function with a loading spinner, errors will trigger a
     * snackbar.
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually load data. It is called in the viewModelScope. Before calling the
     *              lambda the loading spinner will display, after completion or error the loading
     *              spinner will stop
     * @param showSpinner show spinner during async execution
     */
    protected fun awsLambdaFunctionCall(showSpinner: Boolean, block: suspend () -> Unit): Job {
        return viewModelScope.launch {

                if (showSpinner) _spinner.value = true
                    block()
                if (showSpinner) _spinner.value = false
        }
    }

    /**
     * Cancel all coroutines when the ViewModel is cleared.
     */
    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }




}

@BindingAdapter("showProgress")
fun bindProgressBar(progressLayout: FrameLayout, showSpinner: Boolean) {
    if (showSpinner){
        progressLayout.visibility = View.VISIBLE
    } else {
        progressLayout.visibility = View.GONE
    }
}


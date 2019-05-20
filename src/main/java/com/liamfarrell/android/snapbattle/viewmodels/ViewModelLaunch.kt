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
import com.google.gson.JsonParser
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


open class ViewModelLaunch : ViewModel() {

    /**
     * Request a toast to display a string.
     *
     * This variable is private because we don't want to expose MutableLiveData
     *
     * MutableLiveData allows anyone to set a value, and MainViewModel is the only
     * class that should be setting values.
     */
    private val _errorMessage = MutableLiveData<String>()
    /**
     * Request a Toast to display a string.
     */
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _spinner = MutableLiveData<Boolean>()

    val spinner : LiveData<Boolean> = _spinner



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
    protected fun AWSFunctionCall(showSpinner: Boolean, block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                if (showSpinner) _spinner.value = true
                block()
            } catch (error: Error) {
                _errorMessage.value = error.message
            } finally {
                if (showSpinner) _spinner.value = false
            }
        }
    }

    fun handleError(context: Context, error: Error) {
        if (error is AmazonServiceException) {
            _errorMessage.value = context.getString(R.string.server_error_toast)
        } else if (error is LambdaFunctionException) {
            val parser = JsonParser()
            if (parser.parse(error.details).asJsonObject.get("errorType") != null) {
                val errorType = parser.parse(error.details).asJsonObject.get("errorType").asString
                if (errorType == LambdaFunctionsInterface.UPGRADE_REQUIRED_ERROR_MESSAGE) {
                    _errorMessage.value = context.getString(R.string.upgrade_required_toast_message)
                } else if (errorType == ALREADY_FOLLOWING_ERROR) {
                    _errorMessage.value = context.getString(R.string.already_following_error)
                } else {
                    _errorMessage.value = context.getString(R.string.server_error_toast)
                }
            } else {
                _errorMessage.value = context.getString(R.string.server_error_toast)
            }
        } else if (error is AmazonClientException) {
            _errorMessage.value = context.getString(R.string.no_internet_connection_toast)
        }
    }



    companion object {
        val ALREADY_FOLLOWING_ERROR = "ALREADY_FOLLOWING_USER_ERROR"
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

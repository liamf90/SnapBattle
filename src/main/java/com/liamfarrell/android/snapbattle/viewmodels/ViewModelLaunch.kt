package com.liamfarrell.android.snapbattle.viewmodels

import android.view.View
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*

@OpenForTesting
open class ViewModelLaunch : ViewModel() {



    protected val _spinner = MutableLiveData<Boolean>()

    val spinner : LiveData<Boolean> = _spinner

    protected val compositeDisposable = CompositeDisposable()

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
        compositeDisposable.clear()
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


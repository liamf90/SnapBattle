package com.liamfarrell.android.snapbattle.viewmodels.startup

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.UserUpdateRepository
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.LoggedInFragment
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelBase
import javax.inject.Inject

/**
 * The ViewModel used in [ChooseNameStartupFragment].
 */
class ChooseNameStartupViewModel @Inject constructor(private val context : Application, private val userUpdateRepository: UserUpdateRepository) : ViewModelBase() {

    val error = MutableLiveData<Exception>()
    val errorMessage : LiveData<String?> = Transformations.map(error) { error ->
        getErrorMessage(context,  error)
    }

    @SuppressLint("ApplySharedPref")
    fun updateName(newName : String, nextFragmentCallback : ()-> Unit){
        awsLambdaFunctionCall(true,
                suspend { val response = userUpdateRepository.updateName(newName)
                    //Name Updated
                    if (response.error != null) {
                        error.postValue(response.error)
                    } else if (response.result.result == UpdateNameResponse.getNameTooLongErrorCode()){
                        error.postValue(NameTooLongError)
                    } else if (response.result.result == UpdateNameResponse.getResultNameUpdated()){
                        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                        sharedPref.edit().putString(LoggedInFragment.NAME_SHAREDPREFS, newName).commit()
                        //Go to next fragment
                        nextFragmentCallback()
                    }
                })
    }

     object NameTooLongError : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.name_too_long_toast)
    }}
}

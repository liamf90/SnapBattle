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
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.ChooseUsernameStartupFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.LoggedInFragment
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelBase
import javax.inject.Inject

/**
 * The ViewModel used in [FollowFacebookFriendsFragment].
 */
class ChooseUsernameStartupViewModel @Inject constructor(val context : Application, val userUpdateRepository: UserUpdateRepository) : ViewModelBase() {

    val error = MutableLiveData<Exception>()
    val errorMessage : LiveData<String?> = Transformations.map(error) { error ->
        getErrorMessage(context, error)
    }

    @SuppressLint("ApplySharedPref")
    fun updateUsername(newUsername : String, nextActivityCallback : ()->Unit){
        awsLambdaFunctionCall(true,
                suspend { val response = userUpdateRepository.updateUsername(newUsername)
                    //Name Updated

                    if (response.error != null) {
                        error.postValue(response.error)
                    } else if (response.result.result == ChooseUsernameStartupFragment.usernameTooLongErrorCode){
                        error.postValue(UsernameTooLongError)}
                    else if (response.result.result == ChooseUsernameStartupFragment.usernamameNotValidErorrCode){
                        error.postValue(UsernameNotValidError)}
                    else if (response.result.result == ChooseUsernameStartupFragment.usernameUsernameAlreadyExistsErrorCode){
                        error.postValue(UsernameAlreadyExistsError)}
                    else if (response.result.result == UpdateUsernameResponse.getResultUsernameUpdated()){
                        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                        sharedPref.edit().putString(LoggedInFragment.USERNAME_SHAREDPREFS, newUsername).commit()
                        nextActivityCallback()
                    }
                })
    }

    private object UsernameTooLongError : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.username_too_long_toast)
    }}

    private object UsernameNotValidError : CustomError(){
        override fun getErrorToastMessage(context: Context): String {
            return context.getString(R.string.username_wrong_characters_toast)
        }}

    private object UsernameAlreadyExistsError : CustomError(){
        override fun getErrorToastMessage(context: Context): String {
            return context.getString(R.string.username_already_exists_toast)
        }}
}

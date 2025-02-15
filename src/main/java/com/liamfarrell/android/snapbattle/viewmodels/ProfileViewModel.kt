package com.liamfarrell.android.snapbattle.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.api.SnapBattleApiService
import com.liamfarrell.android.snapbattle.data.ProfilePicRepository
import com.liamfarrell.android.snapbattle.data.ProfileRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetProfileResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.ChooseProfilePictureStartupFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.ChooseUsernameStartupFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.LoggedInFragment
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.viewmodels.startup.ChooseNameStartupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel used in [ProfileFragment].
 */
class ProfileViewModel @Inject constructor(private val context: Application, private val snapBattleApiService: SnapBattleApiService,  private val profileRepository: ProfileRepository, private val profilePicRepository : ProfilePicRepository) : ViewModelBase() {

    private val profileResult = MutableLiveData<AsyncTaskResult<GetProfileResponse>>()


    val profile = MediatorLiveData<User>()
    val errorMessage = MediatorLiveData<String>()

    init {
        profile.addSource(profileResult){result ->
            if (result.error == null){
                profile.value = result.result.sqlResult[0]
            }
        }
        errorMessage.addSource(profileResult){result ->
            if (result.error != null){
                errorMessage.value = getErrorMessage(context.applicationContext, result.error)
            }
        }


    }

    fun getProfile(updateProfilePicImage : ()->Unit){
        awsLambdaFunctionCall(true, suspend {
            val profileResponse = profileRepository.getProfile()
            profileResult.value = profileResponse
            if (profileResponse.error == null) {
                val updatedProfilePic = profilePicRepository.checkForUpdate(context, profileResponse.result.sqlResult.get(0).profilePicCount)
                if (updatedProfilePic){
                    updateProfilePicImage()
                } }
        })
    }



    @SuppressLint("ApplySharedPref")
    fun updateName(name: String){
        awsLambdaFunctionCall(true,
                suspend {
                    val response = profileRepository.updateName(name)
                    if (response.error != null) {
                        profileResult.value = AsyncTaskResult(response.error)
                    } else if (response.result.result == UpdateNameResponse.getNameTooLongErrorCode()){
                        profileResult.value = AsyncTaskResult(ChooseNameStartupViewModel.NameTooLongError)
                    } else if (response.result.result == UpdateNameResponse.getResultNameUpdated()){
                        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                        sharedPref.edit().putString(LoggedInFragment.NAME_SHAREDPREFS, name).commit()
                        _snackBarMessage.value = context.getString(R.string.name_updated_snackbar_message)
                    }
                })
    }

    @SuppressLint("ApplySharedPref")
    fun updateUsername(username: String, context: Context){
        awsLambdaFunctionCall(true,
                suspend {
                    val response = profileRepository.updateUsername(username)
                    if (response.error != null) {
                        profileResult.value = AsyncTaskResult(response.error)
                    }

                    if (response.result != null){
                        if (response.result.result == UpdateUsernameResponse.resultUsernameUpdated) {
                            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                            sharedPrefs.edit().putString(LoggedInFragment.USERNAME_SHAREDPREFS, username).commit()
                            _snackBarMessage.value = context.getString(R.string.username_updated_snackbar_message)

                        } else if (response.result.result == ChooseUsernameStartupFragment.usernamameNotValidErorrCode) {
                            profileResult.value = AsyncTaskResult(UsernameNotValid())
                        } else if (response.result.result == ChooseUsernameStartupFragment.usernameTooLongErrorCode) {
                            profileResult.value = AsyncTaskResult(UsernameTooLong())
                        } else if (response.result.result == ChooseUsernameStartupFragment.usernameUsernameAlreadyExistsErrorCode) {
                            profileResult.value = AsyncTaskResult( UsernameAlreadyExists())
                        }
                    }
                }
        )
    }


    fun getProfilePicPath() : String {
        return profilePicRepository.getProfilePictureSavePath(context)
    }

    fun uploadProfilePic(profilePicPath: String) {
        profile.value?.profilePicCount?.let {
        GlobalScope.launch (Dispatchers.IO) {
            val response = profilePicRepository.uploadProfilePicRepository(context,profilePicPath, it)
            if (response.error != null){
                profileResult.value = AsyncTaskResult(response.error)
            } else {
                //Send broadcast to update the profile pic
                val intent = Intent(ChooseProfilePictureStartupFragment.PROFILE_PIC_UPDATED_BROADCAST)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
         }
    } }

}

class UsernameNotValid : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.username_wrong_characters_toast)
    }
}

class UsernameTooLong : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.username_too_long_toast)
    }
}

class UsernameAlreadyExists : CustomError(){
    override fun getErrorToastMessage(context: Context): String {
        return context.getString(R.string.username_already_exists_toast)
    }
}

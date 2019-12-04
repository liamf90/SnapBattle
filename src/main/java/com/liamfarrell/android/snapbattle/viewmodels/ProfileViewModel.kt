package com.liamfarrell.android.snapbattle.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.ProfilePicRepository
import com.liamfarrell.android.snapbattle.data.ProfileRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetProfileResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateUsernameResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.ChooseProfilePictureStartupFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.ChooseUsernameStartupFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.LoggedInFragment
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel used in [ProfileFragment].
 */
class ProfileViewModel @Inject constructor(private val context: Application, private val profileRepository: ProfileRepository, private val profilePicRepository : ProfilePicRepository) : ViewModelLaunch() {

    private val _profile = MutableLiveData<User>()
    val profile : LiveData<User> = _profile

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }


    fun getProfile(updateProfilePicImage : ()->Unit){
        compositeDisposable.add(profileRepository.getProfileRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            _profile.value = onSuccessResponse.sqlResult[0]
                            val updatedProfilePic = profilePicRepository.checkForUpdateRx(context, onSuccessResponse.sqlResult.get(0).profilePicCount)
                            if (updatedProfilePic){
                                updateProfilePicImage()
                            }
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }



    fun updateName(name: String){
        compositeDisposable.add(profileRepository.updateNameRx(name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false

                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }

    @SuppressLint("ApplySharedPref")
    fun updateUsername(username: String, context: Context){
        compositeDisposable.add(profileRepository.updateUsernameRx(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{_spinner.value = true}
                .subscribe(
                        { onSuccessResponse ->
                            _spinner.value = false
                            if (onSuccessResponse.result == UpdateUsernameResponse.resultUsernameUpdated) {
                                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                                sharedPrefs.edit().putString(LoggedInFragment.USERNAME_SHAREDPREFS, username).commit()
                                //update textview in main callbacks showing username
                                //TODO(callbacks as ActivityMainNavigationDrawer).loadUsernameAndName()
                                _snackBarMessage.value = context.getString(R.string.username_updated_snackbar_message)

                            } else if (onSuccessResponse.result == ChooseUsernameStartupFragment.usernamameNotValidErorrCode) {
                                error.value = UsernameNotValid()
                            } else if (onSuccessResponse.result == ChooseUsernameStartupFragment.usernameTooLongErrorCode) {
                                error.value = UsernameTooLong()
                            } else if (onSuccessResponse.result == ChooseUsernameStartupFragment.usernameUsernameAlreadyExistsErrorCode) {
                                error.value =  UsernameAlreadyExists()
                            }
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            error.value = onError
                        }
                ))
    }


    fun getProfilePicPath() : String {
        return profilePicRepository.getProfilePictureSavePath(context)
    }

    fun uploadProfilePic(profilePicPath: String) {
        profile.value?.profilePicCount?.let {
            profilePicRepository.uploadProfilePicRepositoryRx(context,profilePicPath, it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { _spinner.value = true }
                    .subscribe(
                            { _spinner.value = false
                                //Send broadcast to update the profile pic
                                val intent = Intent(ChooseProfilePictureStartupFragment.PROFILE_PIC_UPDATED_BROADCAST)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                            },
                            { onError: Throwable ->
                                _spinner.value = false
                                error.value = onError
                            }
                    )
        }
    }

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

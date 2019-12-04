package com.liamfarrell.android.snapbattle.viewmodels.create_battle

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.ChooseOpponentRepository
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.UpdateNameResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.LoggedInFragment
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch
import com.liamfarrell.android.snapbattle.viewmodels.startup.ChooseNameStartupViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


/**
 * The ViewModel used in [ChooseOpponentFragment].
 */
class ChooseOpponentViewModel @Inject constructor(private val context: Application, val chooseOpponentRepository: ChooseOpponentRepository, val followingRepository : FollowingRepository,
                                                  private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository) : ViewModelLaunch() {

    private val profilePicMap = mutableMapOf<String, String>()

    private var tabIndexSelected = 0
    var searchQuery = ""

    private val _userList = MutableLiveData<List<User>>()
     val userList : LiveData<List<User>> = _userList
    val userListFilteredBySearch = MutableLiveData<List<User>>()


    val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        getErrorMessage(context, it)
    }

    object UsernameNotFoundError : CustomError(){
        override fun getErrorToastMessage(context: Context): String {
            return context.getString(R.string.username_not_found_toast_message)
        }
    }



        fun usernameEnteredManually(username: String, nextFragmentCallback : (user: User) -> Unit) {
            compositeDisposable.add(chooseOpponentRepository.getUsernameToCognitoIdRx(username)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe{_spinner.value = true}
                    .subscribe(
                            { onSuccessResponse ->
                                _spinner.value = false
                                if (onSuccessResponse.sqlResult.isEmpty()){
                                    error.value = UsernameNotFoundError
                                } else {
                                    //go to next fragment
                                    nextFragmentCallback(onSuccessResponse.sqlResult.get(0))
                                }
                            },
                            {onError : Throwable ->
                                _spinner.value = false
                                error.value = onError
                            }
                    ))
        }



        fun followingTabSelected(){
            tabIndexSelected = 0
            _userList.value = listOf()

            compositeDisposable.add( chooseOpponentRepository.getFollowingRx()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnSubscribe{_spinner.value = true}
                    .map { getProfilePicSignedUrls(it.sqlResult).toMutableList() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { onSuccessResponse ->
                                _spinner.value = false
                                if (tabIndexSelected == 0){
                                    _userList.value = onSuccessResponse
                                }
                            },
                            {onError : Throwable ->
                                _spinner.value = false
                                error.value = onError
                            }
                    ))
        }



        fun recentOpponentsTabSelected(){
            tabIndexSelected = 1
            _userList.value = listOf()
            compositeDisposable.add(chooseOpponentRepository.getRecentOpponentsRx()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnSubscribe{_spinner.value = true}
                    .map { getProfilePicSignedUrls(it.sqlResult).toMutableList() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { onSuccessResponse ->
                                _spinner.value = false
                                if (tabIndexSelected == 1){
                                    _userList.value = onSuccessResponse
                                }
                            },
                            {onError : Throwable ->
                                _spinner.value = false
                                error.value = onError
                            }
                    ))
        }

        fun facebookFriendsTabSelected(){
            tabIndexSelected = 2
            _userList.value = listOf()
            compositeDisposable.add(followingRepository.getFacebookFriendsRx()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe{_spinner.value = true}
                    .subscribe(
                            { onSuccessResponse ->
                                _spinner.value = false
                                if (tabIndexSelected == 2){
                                    _userList.value = onSuccessResponse
                                }
                            },
                            {onError : Throwable ->
                                _spinner.value = false
                                error.value = onError
                            }
                    ))
        }

    private fun getProfilePicSignedUrls(userList: List<User>) : List<User>{
        userList.forEach {
            if (it.profilePicCount != 0) {
                if (profilePicMap.containsKey(it.cognitoId)){
                    it.profilePicSignedUrl = profilePicMap[it.cognitoId]
                } else {
                    it.profilePicSignedUrl = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrlRx(it.cognitoId,  it.profilePicCount , it.profilePicSignedUrl )
                }
            }
        }
        return userList
    }


    /**
     *  filters the opponents list with the text the user inputs. results are shown in order of how close the result is.
     *  factors such as a match to the start of the first name will display higher than a match to the part of the last name
     */
     fun filterResults(querySearchView: String) {
        if (querySearchView == "") {
            userListFilteredBySearch.value = _userList.value
            return
        }

        val sortList  = mutableListOf<Pair<Int, User>>()
        _userList.value?.forEach {
            val resultComparison = getSearchMatchRanking(querySearchView.toLowerCase(), it)
            if (resultComparison != 0) {
                sortList.add(Pair(resultComparison,it))}
        }
        sortList.sortBy { it.first }
         userListFilteredBySearch.value = sortList.map { it.second }

    }

    private fun getSearchMatchRanking(querySearch : String, user : User) : Int {
        val query = querySearch.toLowerCase()
        var resultComparison = 0
        val fullNameSplit = user.facebookName.toLowerCase().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toList()
        val username : String? = user.username?.toLowerCase()
        when{
            query == username -> resultComparison = 6 //highest ranking
            fullNameSplit.contains(query) -> resultComparison = 5
            username != null && username.startsWith(query) -> resultComparison = 4
            fullNameSplit.any { it.startsWith(query) } -> resultComparison = 3
            username != null && username.contains(query) -> resultComparison = 2
            fullNameSplit.any{it.contains(query)} -> resultComparison = 1
        }
        return resultComparison
    }
}
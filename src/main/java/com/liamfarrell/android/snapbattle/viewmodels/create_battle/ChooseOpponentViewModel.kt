package com.liamfarrell.android.snapbattle.viewmodels.create_battle

import android.app.Application
import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.ChooseOpponentRepository
import com.liamfarrell.android.snapbattle.data.FollowingRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ResponseFollowing
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.liamfarrell.android.snapbattle.viewmodels.ViewModelLaunch
import javax.inject.Inject


/**
 * The ViewModel used in [ChooseOpponentFragment].
 */
class ChooseOpponentViewModel @Inject constructor(private val context: Application, val chooseOpponentRepository: ChooseOpponentRepository, val followingRepository : FollowingRepository) : ViewModelLaunch() {

    private var tabIndexSelected = 0
    var searchQuery = ""

    private val _userList = MediatorLiveData<List<User>>()
    val userList = _userList

    private val followingResult = MutableLiveData<AsyncTaskResult<ResponseFollowing>>()

    private val recentOpponentsResult = MutableLiveData<AsyncTaskResult<GetUsersResponse>>()

    private val facebookFollowingResult = MutableLiveData<AsyncTaskResult<List<User>>>()



    val errorMessage = MediatorLiveData<String>()

    object UsernameNotFoundError : CustomError(){
        override fun getErrorToastMessage(context: Context): String {
            return context.getString(R.string.username_not_found_toast_message)
        }
    }


    init {
            errorMessage.addSource(followingResult){result ->
                if (result.error != null){
                    errorMessage.value = getErrorMessage(context, result.error) }
            }
            errorMessage.addSource(recentOpponentsResult){result ->
                if (result.error != null){
                    errorMessage.value = getErrorMessage(context, result.error) }
            }
            errorMessage.addSource(facebookFollowingResult){result ->
                if (result.error != null){
                    errorMessage.value = getErrorMessage(context, result.error) }
            }
            _userList.addSource(followingResult){
                if (it.error != null){
                    _userList.value = it.result.sqlResult
                }
            }
            _userList.addSource(recentOpponentsResult){
                if (it.error != null){
                    _userList.value = it.result.sqlResult
                }
            }
            _userList.addSource(facebookFollowingResult){
                if (it.error != null){
                    _userList.value = it.result
                }
            }
        }

        fun usernameEnteredManually(username: String, nextFragmentCallback : (user: User) -> Unit) {
            awsLambdaFunctionCall(true,
                    suspend {
                        val response = chooseOpponentRepository.getUsernameToCognitoId(username)
                        if (response.error != null){
                            errorMessage.postValue(getErrorMessage(context, response.error))
                        } else if (response.result.sqlResult.isEmpty()){
                            errorMessage.postValue(getErrorMessage(context, UsernameNotFoundError))
                        } else {
                            //go to next fragment
                            nextFragmentCallback(response.result.sqlResult.get(0))
                        }
                    })
        }



        fun followingTabSelected(){
            tabIndexSelected = 0
            _userList.value = listOf()
            awsLambdaFunctionCall(true,
                    suspend {
                        val response = chooseOpponentRepository.getFollowing()
                        if (tabIndexSelected == 0){
                            followingResult.value = response
                        } })
        }

        fun recentOpponentsTabSelected(){
            tabIndexSelected = 1
            _userList.value = listOf()
            awsLambdaFunctionCall(true,
                    suspend {
                        val response =  chooseOpponentRepository.getRecentOpponents()
                        if (tabIndexSelected == 1){
                            recentOpponentsResult.value = response
                        }
                    })
        }

        fun facebookFriendsTabSelected(){
            tabIndexSelected = 2
            _userList.value = listOf()
            awsLambdaFunctionCall(true,
                    suspend {
                        val response = followingRepository.getFacebookFriends()
                        if (tabIndexSelected == 2){
                            facebookFollowingResult.value = response
                        }
                    })
        }


    /**
     *  filters the opponents list with the text the user inputs. results are shown in order of how close the result is.
     *  factors such as a match to the start of the first name will display higher than a match to the part of the last name
     */
     fun filterResults(querySearchView: String) {
        val sortMap  = mutableMapOf<Int, User>()
        _userList.value?.forEach {
            val resultComparison = getSearchMatchRanking(querySearchView.toLowerCase(), it)
            if (resultComparison != 0) {
                sortMap.put(resultComparison,it) }
        }
         _userList.value = sortMap.toSortedMap().values.toList()
    }

    private fun getSearchMatchRanking(querySearch : String, user : User) : Int {
        val query = querySearch.toLowerCase()
        var resultComparison = 0
        val fullNameSplit = user.facebookName.toLowerCase().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toList()
        val username = user.username.toLowerCase()
        when{
            query == username -> resultComparison = 1 //highest ranking
            fullNameSplit.contains(query) -> resultComparison = 2
            username.startsWith(query) -> resultComparison = 3
            fullNameSplit.any { it.startsWith(query) } -> resultComparison = 4
            username.contains(query) -> resultComparison = 5
            fullNameSplit.any{it.contains(query)} -> resultComparison = 6
        }
        return resultComparison
    }
}
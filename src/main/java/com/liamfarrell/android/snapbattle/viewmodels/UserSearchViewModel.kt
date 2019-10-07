package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.data.UserSearchRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import javax.inject.Inject
import kotlinx.coroutines.*
import timber.log.Timber


/**
 * The ViewModel used in [UserSearchFragment].
 */
class UserSearchViewModel @Inject constructor(private val context: Application, private val searchRepository: UserSearchRepository,
                                              private val followingUserCacheManager: FollowingUserCacheManager, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository
                                                 ) : ViewModelLaunch() {

    enum class State{
        CACHE_RESULT,
        SERVER_SEARCH,
        SERVER_AND_CACHE_RESULT
    }

     val searchState = MutableLiveData<State>()

    private val profilePicMap = mutableMapOf<String, String>()

    private val searchResultResponse = MutableLiveData<AsyncTaskResult<GetUsersResponse>>()

    private var searchQueryCurrent = ""

    val searchResult : LiveData<List<User>> =  Transformations.map(searchResultResponse) { asyncResult ->
        asyncResult.result.sqlResult }

    val errorMessage : LiveData<String?> = Transformations.map(searchResultResponse) { asyncResult ->
        if (asyncResult.error != null){
        getErrorMessage(context, asyncResult.error)}
        else null
    }

    init {
        searchState.value = State.CACHE_RESULT
        GlobalScope.launch {followingUserCacheManager.checkForUpdates()}
    }



    private var submitPressed = false
    private var searchingCache = false
    private var doServerSearchOverride = false


    /**
     * When the search query is changed, for any query length > 0, a search is done for the users in the FollowingCacheDatabase.
     * When the query length >= 3, a search is done on the server as well, with the the result appended to the Following Cache search users for distinct users only
     */
    fun searchQueryChange(searchQuery: String) {
        doServerSearchOverride = false
        searchQueryCurrent = searchQuery
        //clear the list
        //searchResultResponse.value?.result?.sqlResult?.clear()
        //searchResultResponse.value = AsyncTaskResult(GetUsersResponse())

        if (searchQuery.isEmpty()){
            searchResultResponse.value = AsyncTaskResult(GetUsersResponse())
            return}

        searchingCache = true
        val searchCacheJob = getSearchFollowingCacheJobAsync(searchQuery)

        viewModelScope.launch {
            var serverSearchJob =
                if (searchQuery.length >= 3) {
                     searchState.value = State.SERVER_SEARCH
                     getServerSearchJobAsync(searchQuery)
                } else {
                     searchState.value = State.CACHE_RESULT
                     null
                }

            val followingResponse = searchCacheJob.await()

            //Only continue if the search query has not been changed
            if (searchQuery == searchQueryCurrent){
                searchResultResponse.value = followingResponse
                searchingCache = false
                //doServerSearchOverride will be set to true when the search submit button is pressed and query length < 3 and the FollowingCache search has not yet been finished.
                //In this case do the server search job now
                if (doServerSearchOverride){
                    serverSearchJob = getServerSearchJobAsync(searchQuery)
                }
                val searchUserResponse = serverSearchJob?.await()
                if (searchUserResponse != null && searchQuery == searchQueryCurrent){
                    val combinedList = addServerSearchToCacheList(followingResponse.result.sqlResult, searchUserResponse.result.sqlResult)
                    searchUserResponse.result.sqlResult = combinedList
                    searchUserResponse.result.sqlResult = getProfilePicSignedUrls(combinedList)
                    searchState.value = State.SERVER_AND_CACHE_RESULT
                    searchResultResponse.value = searchUserResponse
                }
            }
            searchingCache = false
        }
    }


    /**
     * Method called when the search submit button is pressed
     * Since searchQueryChange method only runs the server search when search query length >= 3,
     * this method forces the server search to be run when the query length < 3
     */
    fun searchUserSubmit(searchQuery : String){

        submitPressed = true
        //if searchQuery.length < 3 do a server search on search submit else it will occur anyway on search text changed
        //else return
        if (searchQuery.length >= 3){return}

        //if currently searching following cache in searchQueryChange, set doServerSearchOverride = true so the searchQueryChange will do the server search after searching the following cache
        //if the searchQueryChange method has finished searching the following cache, search the server in this method
        if (searchingCache){
            doServerSearchOverride = true
        } else {
            viewModelScope.launch {
                searchState.value = State.SERVER_SEARCH
                val serverJob = getServerSearchJobAsync(searchQuery)
                val response = serverJob.await()
                if (response != null && searchQuery == searchQueryCurrent){
                    val cacheList = searchResultResponse.value?.result?.sqlResult
                    if (cacheList != null){
                        val combinedList = addServerSearchToCacheList(cacheList, response.result.sqlResult)
                        response.result.sqlResult = combinedList
                        searchState.value = State.SERVER_AND_CACHE_RESULT
                        searchResultResponse.value = response

                    }
                } else {
                    searchState.value = State.CACHE_RESULT
                }
            }
        }

    }

    /**
     * Appends the server search list to the following cache list, but only with distinct Users
     */
    private fun addServerSearchToCacheList(cacheList : List<User>, serverList : List<User>) : List<User>{
        val cognitoIDCacheList = cacheList.map { it.cognitoId }
        val serverListFiltered = serverList.filterNot {cognitoIDCacheList.contains(it.cognitoId)}
        val finalList =  mutableListOf<User>()
        finalList.addAll(cacheList)
        finalList.addAll(serverListFiltered)
        return finalList
    }


    private fun getServerSearchJobAsync(searchQuery : String) = viewModelScope.async {
            submitPressed = false
            //debounce the search. if user pressed the submit search button, the debounce wait will stop and the search will occur straight away
            var delayCountMilliSeconds = 0
            while (!submitPressed && delayCountMilliSeconds < 300){
                delay(10)
                delayCountMilliSeconds += 10
            }
            submitPressed = false
            //only do search if the search query is still the same
            if (searchQuery == searchQueryCurrent){
                return@async searchRepository.searchUser(searchQuery)
            } else {
                return@async null
            }
    }


    private fun getSearchFollowingCacheJobAsync(searchQuery: String) =
         viewModelScope.async {
             val getUsersResponse = GetUsersResponse()
             getUsersResponse.sqlResult = followingUserCacheManager.searchUsersInCache(searchQuery)
             AsyncTaskResult(getUsersResponse)
    }

    private suspend fun getProfilePicSignedUrls(userList: List<User>) : List<User>{
        userList.forEach {
            if (it.profilePicCount != 0) {
                if (profilePicMap.containsKey(it.cognitoId)){
                    it.profilePicSignedUrl = profilePicMap[it.cognitoId]
                } else {
                    it.profilePicSignedUrl = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrl(it.cognitoId,  it.profilePicCount , it.profilePicSignedUrl )
                }
            }
        }
        return userList
    }


}
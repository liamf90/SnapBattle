package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.data.UserSearchRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersResponse
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import kotlinx.coroutines.*
import javax.inject.Inject


/**
 * The ViewModel used in [UserSearchFragment].
 */
class UserSearchViewModel @Inject constructor(private val context: Application, private val searchRepository: UserSearchRepository,
                                              private val followingUserCacheManager: FollowingUserCacheManager, private val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository
                                                 ) : ViewModelBase() {

    enum class State{
        CACHE_RESULT,
        SERVER_SEARCH,
        SERVER_AND_CACHE_RESULT
    }

    private val _searchState = MutableLiveData<State>()
    val searchState : LiveData<State> = _searchState

    private val profilePicMap = mutableMapOf<String, String>()


    private val followingCacheSearchResult = MutableLiveData<List<User>>()
    private  val serverSearchCacheSearchResult = MutableLiveData<List<User>>()
    private val  _searchList = MediatorLiveData<List<User>>()
    val searchList : LiveData<List<User>> = _searchList

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        it.printStackTrace()
        getErrorMessage(context, it)
    }

    private var searchQueryCurrent = ""
    private var submitPressed = false


    private var  searchCacheJob : Job? = null
    private var searchServerJob : Job? = null


    init {
        _searchState.value = State.CACHE_RESULT
        GlobalScope.launch {followingUserCacheManager.checkForUpdates()}

        _searchList.addSource(followingCacheSearchResult){
            it?.let {
                if (serverSearchCacheSearchResult.value != null){
                    val combinedList = addServerSearchToCacheList(it, serverSearchCacheSearchResult.value ?: listOf())
                    _searchList.value = combinedList
                } else {
                    _searchList.value = it
                }
            }
        }

        _searchList.addSource(serverSearchCacheSearchResult){
            it?.let{
                _searchState.value = State.SERVER_AND_CACHE_RESULT
                if (followingCacheSearchResult.value != null){
                    val combinedList = addServerSearchToCacheList(followingCacheSearchResult.value ?: listOf(), it)
                    _searchList.value = combinedList
                } else {
                    _searchList.value = it
                }
            }
        }
    }




    /**
     * When the search query is changed, for any query length > 0, a search is done for the users in the FollowingCacheDatabase.
     * When the query length >= 3, a search is done on the server as well, with the the result appended to the Following Cache search users for distinct users only
     */
    fun searchQueryChange(searchQuery: String) {


        searchServerJob?.cancel()
        searchCacheJob?.cancel()

        _searchState.value = State.CACHE_RESULT
        searchQueryCurrent = searchQuery
        _searchList.value = null
        followingCacheSearchResult.value = null
        serverSearchCacheSearchResult.value = null

        if (searchQuery.isEmpty()){ return}



        searchCacheJob = viewModelScope.launch {
            val cacheSearchList = withContext(Dispatchers.IO){
                followingUserCacheManager.searchUsersInCache(searchQuery)
            }
            followingCacheSearchResult.value  = cacheSearchList
        }


        if (searchQuery.length >= 3) {
            searchServerJob = viewModelScope.launch {
                _searchState.value = State.SERVER_SEARCH
                val response = getServerSearchJob(searchQuery)
                if (response.error == null) {
                    val serverList = getProfilePicSignedUrls(response.result.sqlResult)
                    _searchState.value = State.SERVER_AND_CACHE_RESULT
                    serverSearchCacheSearchResult.value = serverList
                } else {
                    //error
                    error.value = response.error
                }
            }
        } else {
            _searchState.value = State.CACHE_RESULT
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
        if (searchQuery.length >= 3 || searchQuery.isEmpty()){return}


        searchServerJob = viewModelScope.launch {
            _searchState.value = State.SERVER_SEARCH
            val serverJob = searchRepository.searchUser(searchQuery)
            if (serverJob.error == null){
                val serverList = getProfilePicSignedUrls(serverJob.result.sqlResult)
                _searchState.value = State.SERVER_AND_CACHE_RESULT
                serverSearchCacheSearchResult.value = serverList
            } else {
                //error
                error.value = serverJob.error
            }
        }



    }

    /**
     * Appends the server search list to the following cache list, but only with distinct Users
     */
    private fun addServerSearchToCacheList(cacheList : List<User>, serverList : List<User>) : List<User>{
        val cognitoIDCacheList = cacheList.map { it.cognitoId }
        cacheList.forEach {
            it.profilePicSignedUrl = serverList.find { user-> user.cognitoId == it.cognitoId }?.profilePicSignedUrl
            it.profilePicCount = serverList.find { user-> user.cognitoId == it.cognitoId }?.profilePicCount ?: 0
        }
        val serverListFiltered = serverList.filterNot {cognitoIDCacheList.contains(it.cognitoId)}
        val finalList =  mutableListOf<User>()
        finalList.addAll(cacheList)
        finalList.addAll(serverListFiltered)
        return finalList
    }


    private suspend fun getServerSearchJob(searchQuery : String) : AsyncTaskResult<GetUsersResponse> {
            submitPressed = false
            //debounce the search. if user pressed the submit search button, the debounce wait will stop and the search will occur straight away
            var delayCountMilliSeconds = 0
            while (!submitPressed && delayCountMilliSeconds < 300){
                delay(10)
                delayCountMilliSeconds += 10
            }
            submitPressed = false

            return searchRepository.searchUser(searchQuery)

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
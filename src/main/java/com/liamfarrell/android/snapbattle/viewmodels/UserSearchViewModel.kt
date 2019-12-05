package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.liamfarrell.android.snapbattle.data.FollowingUserCacheManager
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.data.UserSearchRepository
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.*
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

    private val _searchState = MutableLiveData<State>()
    val searchState : LiveData<State> = _searchState

    private val profilePicMap = mutableMapOf<String, String>()

    private var searchQueryCurrent = ""

    private val followingCacheSearchResult = MutableLiveData<List<User>>()
    private  val serverSearchCacheSearchResult = MutableLiveData<List<User>>()
    private val  _searchList = MediatorLiveData<List<User>>()
    val searchList : LiveData<List<User>> = _searchList

    private val error = MutableLiveData<Throwable>()
    val errorMessage : LiveData<String> = Transformations.map(error){
        it.printStackTrace()
        getErrorMessage(context, it)
    }

    private var submitPressed = false


    init {
        _searchState.value = State.CACHE_RESULT
        followingUserCacheManager.checkForUpdates().subscribeOn(io()).subscribe()

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
        compositeDisposable.clear()
        searchQueryCurrent = searchQuery
        _searchList.value = null
        followingCacheSearchResult.value = null
        serverSearchCacheSearchResult.value = null
        _searchState.value = State.CACHE_RESULT

        if (searchQuery.isEmpty()){ return}

        //search the cache
        compositeDisposable.add(followingUserCacheManager.searchUsersInCacheRx(searchQuery)
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            followingCacheSearchResult.value = onSuccessResponse
                        },
                        {onError : Throwable ->
                            error.value = onError
                        }
                ))



        //search server if query length >=3
        if (searchQuery.length >= 3) {
            compositeDisposable.add(
                    getDebounceWaitCompletable()
                    .subscribeOn(newThread())
                    .andThen(searchRepository.searchUserRx(searchQuery))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        _searchState.value = State.SERVER_SEARCH
                    }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(single())
                    .map {getProfilePicSignedUrls(it.sqlResult) }
                    .observeOn(newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(io())
                    .subscribe(
                            { onSuccessResponse ->
                                serverSearchCacheSearchResult.value = onSuccessResponse
                            },
                            { onError: Throwable ->
                                error.value = onError
                            }
                    ))
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


        compositeDisposable.add(searchRepository.searchUserRx(searchQuery)
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _searchState.value = State.SERVER_SEARCH
                }
                .observeOn(single())
                .map {
                    getProfilePicSignedUrls(it.sqlResult) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccessResponse ->
                            serverSearchCacheSearchResult.value = onSuccessResponse
                        },
                        { onError: Throwable ->
                            error.value = onError
                        }
                ))

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

    private fun getDebounceWaitCompletable() : Completable {
        return Completable.create {
            submitPressed = false
            var delayCountMilliSeconds = 0
            while (!submitPressed && delayCountMilliSeconds < 2000) {
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException){ }
                delayCountMilliSeconds += 10
                Timber.i("DelayCount %s", delayCountMilliSeconds)
            }
            submitPressed = false
            it.onComplete()
        }

    }




    private  fun getProfilePicSignedUrls(userList: List<User>) : List<User>{
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


}
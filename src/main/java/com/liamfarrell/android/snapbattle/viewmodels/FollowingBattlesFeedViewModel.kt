package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.FollowingBattlesFeedRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.db.FollowingBattle
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.FollowingBattlesSearchResult
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * The ViewModel used in [FollowingBattlesFeedFragment].
 */
class FollowingBattlesFeedViewModel @Inject constructor(private val context: Application, private val followingBattlesFeedRepository: FollowingBattlesFeedRepository, val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelLaunch() {

    private val allBattlesResult = MutableLiveData<FollowingBattlesSearchResult>()
    private val _noMoreOlderBattles =  followingBattlesFeedRepository.isNoMoreOlderBattles
    private val _loadingMoreBattles = followingBattlesFeedRepository.isLoadingMoreBattles

    val battles: LiveData<PagedList<FollowingBattle>> = Transformations.switchMap(allBattlesResult) { it -> it.data }
    private val networkErrors: LiveData<Throwable> = Transformations.switchMap(allBattlesResult) { it ->
        it.networkErrors
    }

    val errorMessage : LiveData<String> = Transformations.map(networkErrors){
        it.printStackTrace()
        it?.let{getErrorMessage(context, it)}
    }


    val noMoreOlderBattles : LiveData<Boolean> = _noMoreOlderBattles
    val isLoadingMoreBattles : LiveData<Boolean> = _loadingMoreBattles
    val isNoBattlesInFeed = Transformations.map(_noMoreOlderBattles) {
        it && battles.value?.size == 0
    }


    val battleIdsOfThumbnailsLoadedList = mutableListOf<Int>()



    init {
        allBattlesResult.value = followingBattlesFeedRepository.loadAllBattles(compositeDisposable)
        updateFollowingBattlesRepeating()
    }

    private fun updateFollowingBattlesRepeating() {
        compositeDisposable.add(
                Observable.interval(0, HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .concatMapCompletable { followingBattlesFeedRepository.updateBattles()}
                        .subscribe(
                                {},
                                {onError : Throwable ->
                                    _snackBarMessage.value = getErrorMessage(context, onError)
                                }
                        ))
    }

    fun updateFollowingBattles(){
        compositeDisposable.add(followingBattlesFeedRepository.updateBattles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { _spinner.value = true }
                .subscribe(
                        { //onComplete
                            _spinner.value = false
                        },
                        {onError : Throwable ->
                            _spinner.value = false
                            _snackBarMessage.value = getErrorMessage(context, onError)
                        }
                ))
    }

    fun loadThumbnail(b: Battle){
        if (battleIdsOfThumbnailsLoadedList.contains(b.battleId)) return


        //thumbnail signed url repository will update the signed url for the thumbnail in the room db if needed (not still in picasso cache, not loaded yet etc)
        //this change will be reflected in the left join of the LiveData of the getAllBattles Dao call and the thumbnail will be auto updated
        battleIdsOfThumbnailsLoadedList.add(b.battleID)
        compositeDisposable.add(Completable.fromCallable {thumbnailSignedUrlCacheRepository.getThumbnailSignedUrlRx(b)}.subscribeOn(Schedulers.io()).subscribe())
    }

    companion object{
        val HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS : Long = 60000; // EVERY 60 SECONDS
    }


}
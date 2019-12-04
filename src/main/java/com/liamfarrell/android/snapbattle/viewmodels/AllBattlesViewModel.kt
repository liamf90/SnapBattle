package com.liamfarrell.android.snapbattle.viewmodels


import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.AllBattlesRepository
import com.liamfarrell.android.snapbattle.data.ThumbnailSignedUrlCacheRepository
import com.liamfarrell.android.snapbattle.db.AllBattlesBattle
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.BattlesSearchResult
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


/**
 * The ViewModel used in [AllBattlesFragment].
 */
class AllBattlesViewModel @Inject constructor(private val context: Application, private val allBattlesRepository: AllBattlesRepository, val thumbnailSignedUrlCacheRepository: ThumbnailSignedUrlCacheRepository) : ViewModelLaunch() {

    private val allBattlesResult = MutableLiveData<BattlesSearchResult>()
    private val _noMoreOlderBattles =  allBattlesRepository.isNoMoreOlderBattles
    private val _loadingMoreBattles = allBattlesRepository.isLoadingMoreBattles

    val battles: LiveData<PagedList<AllBattlesBattle>> = Transformations.switchMap(allBattlesResult) { it -> it.data }
    private val networkErrors: LiveData<Throwable> = Transformations.switchMap(allBattlesResult) { it ->
        it.networkErrors
    }

    val errorMessage : LiveData<String> = Transformations.map(networkErrors){
        it.printStackTrace()
        it?.let{getErrorMessage(context, it)}
    }


    val noMoreOlderBattles : LiveData<Boolean> = _noMoreOlderBattles
    val isLoadingMoreBattles : LiveData<Boolean> = _loadingMoreBattles

    val battleIdsOfThumbnailsLoadedList = mutableListOf<Int>()




    init {
        allBattlesResult.value = allBattlesRepository.loadAllBattles(compositeDisposable)
        updateAllBattlesRepeating()
    }

    private fun updateAllBattlesRepeating() {
        compositeDisposable.add(
                Observable.interval(0, HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS, TimeUnit.MILLISECONDS)
                        .subscribeOn(io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .concatMapCompletable { allBattlesRepository.updateBattles() }
                        .subscribe(
                                {},
                                {onError : Throwable ->
                                    _snackBarMessage.value = getErrorMessage(context, onError)
                                }
                        ))
    }

    fun updateAllBattles() {
        compositeDisposable.add(allBattlesRepository.updateBattles()
                .subscribeOn(io())
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
        compositeDisposable.add(Completable.fromCallable {thumbnailSignedUrlCacheRepository.getThumbnailSignedUrlRx(b)}.subscribeOn(io()).subscribe())

    }

    companion object{
        val HOW_OFTEN_CHECK_FOR_UPDATES_MILLISECONDS : Long = 60000; // EVERY 60 SECONDS
    }


}
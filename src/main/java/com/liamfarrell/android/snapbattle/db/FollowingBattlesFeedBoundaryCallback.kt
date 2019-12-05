package com.liamfarrell.android.snapbattle.db

import androidx.paging.PagedList
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.data.AllBattlesCacheManager
import com.liamfarrell.android.snapbattle.data.FollowingBattlesFeedCacheManager
import com.liamfarrell.android.snapbattle.model.Battle
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This boundary callback gets notified when user reaches to the edges of the list for example when
 * the database cannot provide any more data.
 **/
class FollowingBattlesFeedBoundaryCallback(
        private val followingBattlesFeedCacheManager: FollowingBattlesFeedCacheManager,
        private val compositeDisposable: CompositeDisposable,
        private val errorLiveData : MutableLiveData<Throwable>
) : PagedList.BoundaryCallback<FollowingBattle>() {


    override fun onZeroItemsLoaded() {
    }

    /**
     * When all items in the database were loaded, we need to query the backend for more items.
     */
    override fun onItemAtEndLoaded(itemAtEnd: FollowingBattle) {
        compositeDisposable.add(Completable.fromCallable {followingBattlesFeedCacheManager.requestMoreBattles()}.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({},{onError: Throwable ->
                    errorLiveData.value = onError}))
    }

}
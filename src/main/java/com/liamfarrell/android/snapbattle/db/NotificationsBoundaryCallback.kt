package com.liamfarrell.android.snapbattle.db

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.data.NotificationsManager
import com.liamfarrell.android.snapbattle.notifications.NotificationDb
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * This boundary callback gets notified when user reaches to the edges of the list for example when
 * the database cannot provide any more data.
 **/
class NotificationsBoundaryCallback(
        private val notificationsManager: NotificationsManager,
        private val compositeDisposable: CompositeDisposable,
        private val errorLiveData : MutableLiveData<Throwable>
        ) : PagedList.BoundaryCallback<NotificationDb>() {


    override fun onZeroItemsLoaded() {
        Timber.d("onZeroItemsLoaded")
    }

    /**
     * When all items in the database were loaded, we need to query the backend for more items.
     */
    override fun onItemAtEndLoaded(itemAtEnd: NotificationDb) {
        compositeDisposable.add(Completable.fromCallable { notificationsManager.requestMoreNotifications()}.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({},{onError: Throwable ->
                    errorLiveData.value = onError}))
    }

}
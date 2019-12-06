package com.liamfarrell.android.snapbattle.app

import android.app.Activity
import android.app.Application
import android.content.Context
import com.liamfarrell.android.snapbattle.BuildConfig
import com.liamfarrell.android.snapbattle.di.AppInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.internal.functions.Functions.emptyConsumer
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

class SnapBattleApp : Application(), HasActivityInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        //ensure that errors are only handled by the onError() consumer of the observer,  set the global handler to an empty consumer:
        RxJavaPlugins.setErrorHandler(emptyConsumer())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        AppInjector.init(this)
    }

    override fun activityInjector() = dispatchingAndroidInjector


}

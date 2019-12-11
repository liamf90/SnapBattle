package com.liamfarrell.android.snapbattle.app

import android.app.Activity
import android.app.Application
import android.content.Context
import com.liamfarrell.android.snapbattle.BuildConfig
import com.liamfarrell.android.snapbattle.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class SnapBattleApp : Application(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        AppInjector.init(this)
    }


    override fun androidInjector() = dispatchingAndroidInjector
}

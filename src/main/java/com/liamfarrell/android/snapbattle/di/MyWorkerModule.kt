package com.liamfarrell.android.snapbattle.di

import androidx.work.CoroutineWorker
import androidx.work.Worker
import com.liamfarrell.android.snapbattle.workers.ChildWorkerFactory
import com.liamfarrell.android.snapbattle.workers.FollowingUserCacheDatabaseWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out CoroutineWorker>)

@Module
abstract class MyWorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(FollowingUserCacheDatabaseWorker::class)
    internal abstract fun bindMyWorkerFactory(worker: FollowingUserCacheDatabaseWorker.Factory): ChildWorkerFactory
}
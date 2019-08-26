package com.liamfarrell.android.snapbattle.di

import com.liamfarrell.android.snapbattle.viewmodel.BattlesReportedViewModelFactory
import com.liamfarrell.android.snapbattle.viewmodel.CommentsReportedViewModelFactory
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [BattlesReportedViewModelFactoryModule::class])
interface BattlesReportedComponent {
    fun getBattlesReportedViewModelFactory() : BattlesReportedViewModelFactory
}

@Singleton
@Component(modules = [CommentsReportedViewModelFactoryModule::class])
interface CommentsReportedComponent {
    fun getCommentsReportedViewModelFactory() : CommentsReportedViewModelFactory
}


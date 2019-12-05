package com.liamfarrell.android.snapbattle.di

import android.app.Application
import android.content.Context
import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import com.liamfarrell.android.snapbattle.viewmodel.BattlesReportedViewModelFactory
import com.liamfarrell.android.snapbattle.viewmodel.CommentsReportedViewModelFactory
import dagger.Module
import dagger.Provides


@Module(includes = [RepositoryModuleAdmin::class])
class CommentsReportedViewModelFactoryModule {
    @Provides
    fun commentsReportedViewModelFactory(context: Application, reportingsRepository: ReportingsRepository) : CommentsReportedViewModelFactory {
        return CommentsReportedViewModelFactory(context, reportingsRepository)
    }
}

@Module(includes = [RepositoryModuleAdmin::class])
class BattlesReportedViewModelFactoryModule {
    @Provides
    fun battlesReportedViewModelFactory(context: Application, reportingsRepository: ReportingsRepository) : BattlesReportedViewModelFactory {
        return BattlesReportedViewModelFactory(context, reportingsRepository)
    }
}


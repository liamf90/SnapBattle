package com.liamfarrell.android.snapbattle.di

import com.liamfarrell.android.snapbattle.data.ReportingsRepository
import com.liamfarrell.android.snapbattle.viewmodel.BattlesReportedViewModelFactory
import com.liamfarrell.android.snapbattle.viewmodel.CommentsReportedViewModelFactory
import dagger.Module
import dagger.Provides


@Module(includes = [RepositoryModuleAdmin::class])
class CommentsReportedViewModelFactoryModule {
    @Provides
    fun commentsReportedViewModelFactory(reportingsRepository: ReportingsRepository) : CommentsReportedViewModelFactory {
        return CommentsReportedViewModelFactory(reportingsRepository)
    }
}

@Module(includes = [RepositoryModuleAdmin::class])
class BattlesReportedViewModelFactoryModule {
    @Provides
    fun battlesReportedViewModelFactory(reportingsRepository: ReportingsRepository) : BattlesReportedViewModelFactory {
        return BattlesReportedViewModelFactory(reportingsRepository)
    }
}


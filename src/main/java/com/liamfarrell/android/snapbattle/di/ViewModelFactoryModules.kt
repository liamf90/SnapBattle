package com.liamfarrell.android.snapbattle.di

import android.content.Context
import com.liamfarrell.android.snapbattle.data.*
import com.liamfarrell.android.snapbattle.data.following_battle_feed.FollowingBattlesFeedRepository
import com.liamfarrell.android.snapbattle.viewmodels.*
import dagger.Module
import dagger.Provides

@Module(includes = [RepositoryModule::class])
class FollowingViewModelFactoryModule()  {
    @Provides
    fun followViewModelFactory( followingRepository: FollowingRepository) : FollowingViewModelFactory {
        return FollowingViewModelFactory(followingRepository)
    }
}


@Module(includes = [RepositoryModule::class])
class FollowFacebookFriendViewModelFactoryModule(val  requestFacebookFriendsPermission : ()->Unit )  {

    @Provides
    fun followFacebookFriendViewModelFactory( followingRepository: FollowingRepository ) : FollowFacebookFriendsViewModelFactory {
        return FollowFacebookFriendsViewModelFactory(followingRepository, requestFacebookFriendsPermission)
    }
}

@Module(includes = [RepositoryModule::class])
class CompletedBattlesViewModelFactoryMoodule  {
    @Provides
    fun completedBattlesViewModelFactory( completedBattlesRepository: CompletedBattlesRepository) : CompletedBattlesViewModelFactory {
        return CompletedBattlesViewModelFactory(completedBattlesRepository)
    }
}

@Module(includes = [RepositoryModule::class])
class CurrentBattlesViewModelFactoryMoodule  {
    @Provides
    fun completedBattlesViewModelFactory( currentBattlesRepository: CurrentBattlesRepository) : CurrentBattlesViewModelFactory {
        return CurrentBattlesViewModelFactory(currentBattlesRepository)
    }
}


@Module(includes = [AllBattlesRepositoryModule::class])
class AllBattlesViewModelFactoryModule()  {
    @Provides
    fun allBattlesViewModelFactory( allBattlesRepository: AllBattlesRepository) : AllBattlesViewModelFactory {
        return AllBattlesViewModelFactory(allBattlesRepository)
    }
}

@Module(includes = [FollowingBattlesFeedRepositoryModule::class])
class FollowingBattlesFeedViewModelFactoryModule  {
    @Provides
    fun followingBattlesFeedViewModelFactory( followingBattlesFeedRepository: FollowingBattlesFeedRepository) : FollowingBattlesFeedViewModelFactory {
        return FollowingBattlesFeedViewModelFactory(followingBattlesFeedRepository)
    }
}


@Module(includes = [RepositoryModule::class])
class BattlesByNameViewModelFactoryModule(val battleName: String) {
    @Provides
    fun battlesByNameViewModelFactory(battlesRepository: BattlesFromNameRepository): BattlesByNameViewModelFactory {
        return BattlesByNameViewModelFactory(battlesRepository, battleName)
    }
}

@Module(includes = [RepositoryModule::class])
class ViewOwnBattleViewModelFactoryModule(val battleID: Int) {
    @Provides
    fun viewOwnBattleViewModelFactory(usersBattleRepository: UsersBattleRepository): ViewOwnBattleViewModelFactory {
        return ViewOwnBattleViewModelFactory(battleID, usersBattleRepository)
    }
}

@Module(includes = [RepositoryModule::class])
class BattleNameSearchViewModelFactoryModule {
    @Provides
    fun battleNameSearchViewModelFactory(battleNameSearchRepository: BattleNameSearchRepository): BattleNameSearchViewModelFactory {
        return BattleNameSearchViewModelFactory(battleNameSearchRepository)
    }
}

@Module(includes = [RepositoryModule::class, UserSearchRepositoryModule::class])
class UserSearchViewModelFactoryModule() {
    @Provides
    fun userSearchViewModelFactory(userSearchRepository: UserSearchRepository, followingUserCacheManager: FollowingUserCacheManager): UserSearchViewModelFactory {
        return UserSearchViewModelFactory(userSearchRepository, followingUserCacheManager)
    }
}





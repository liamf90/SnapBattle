/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//class copied from https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample/app/src/main/java/com/android/example/github/di/ViewModelModule.kt

package com.liamfarrell.android.snapbattle.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.viewmodels.*
import com.liamfarrell.android.snapbattle.viewmodels.create_battle.ChooseBattleTypeViewModel
import com.liamfarrell.android.snapbattle.viewmodels.create_battle.ChooseOpponentViewModel
import com.liamfarrell.android.snapbattle.viewmodels.startup.AddFacebookFriendsAsFollowersStartupViewModel
import com.liamfarrell.android.snapbattle.viewmodels.startup.ChooseNameStartupViewModel
import com.liamfarrell.android.snapbattle.viewmodels.startup.ChooseUsernameStartupViewModel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AllBattlesViewModel::class)
    abstract fun bindUserViewModel(allBattlesViewModel: AllBattlesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BattleChallengesViewModel::class)
    abstract fun bindBattleChallengesViewModel(battleChallengesViewModel: BattleChallengesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BattleNameSearchViewModel::class)
    abstract fun bindBattleNameViewModel(battleNameSearchViewModel: BattleNameSearchViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(CommentViewModel::class)
    abstract fun bindCommentViewModel(repoViewModel: CommentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CompletedBattlesViewModel::class)
    abstract fun bindCompletedBattlesViewModel(completedBattlesViewModel: CompletedBattlesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CurrentBattlesViewModel::class)
    abstract fun bindCurrentBattlesViewModel(currentBattlesViewModel: CurrentBattlesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowFacebookFriendsViewModel::class)
    abstract fun bindFollowFacebookFriendsViewModel(followFacebookFriendsViewModel: FollowFacebookFriendsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowingBattlesFeedViewModel::class)
    abstract fun bindFollowingBattlesFeedViewModel(followingBattlesFeedViewModel: FollowingBattlesFeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowingViewModel::class)
    abstract fun bindFollowingViewModel(followingViewModel: FollowingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationsViewModel::class)
    abstract fun bindNotificationsViewModel(notificationsViewModel: NotificationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UsersBattlesViewModel::class)
    abstract fun bindUsersBattlesViewModel(usersBattlesViewModel: UsersBattlesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserSearchViewModel::class)
    abstract fun bindUserSearchViewModel(userSearchViewModel: UserSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VerifyBattleViewModel::class)
    abstract fun bindVerifyBattleViewModel(verifyBattleViewModel: VerifyBattleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddFacebookFriendsAsFollowersStartupViewModel::class)
    abstract fun bindAddFacebookFriendsAsFollowersStartupViewModel(addFacebookFriendsAsFollowersStartupViewModel: AddFacebookFriendsAsFollowersStartupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChooseNameStartupViewModel::class)
    abstract fun bindChooseNameStartupViewModel(chooseNameStartupViewModel: ChooseNameStartupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChooseUsernameStartupViewModel::class)
    abstract fun bindChooseUsernameStartupViewModel(chooseUsernameStartupViewModel: ChooseUsernameStartupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChooseBattleTypeViewModel::class)
    abstract fun bindChooseBattleTypeViewModel(chooseBattleTypeViewModel: ChooseBattleTypeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChooseOpponentViewModel::class)
    abstract fun bindChooseOpponentViewModel(chooseOpponentViewModel: ChooseOpponentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewOwnBattleViewModel::class)
    abstract fun bindViewOwnBattleViewModel(viewOwnBattleViewModel: ViewOwnBattleViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: SnapBattleViewModelFactory): ViewModelProvider.Factory
}

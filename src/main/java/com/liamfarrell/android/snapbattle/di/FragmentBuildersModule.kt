/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.liamfarrell.android.snapbattle.di

import com.liamfarrell.android.snapbattle.mvvm_ui.*
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseBattleTypeFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseOpponentFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.VerifyBattleFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.startup.*
import com.liamfarrell.android.snapbattle.ui.FullBattleVideoPlayerFragment


import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeAllBattlesFragment(): AllBattlesFragment

    @ContributesAndroidInjector
    abstract fun contributeBattleChallengesListFragment(): BattleChallengesListFragment

    @ContributesAndroidInjector
    abstract fun contributeBattleCompletedListFragment(): BattleCompletedListFragment

    @ContributesAndroidInjector
    abstract fun contributeBattleCurrentListFragment(): BattleCurrentListFragment

    @ContributesAndroidInjector
    abstract fun contributeBattleNameSearchFragment(): BattleNameSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeBattlesFromNameFragment(): BattlesFromNameFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowFacebookFriendsFragment(): FollowFacebookFriendsFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowingBattlesFeedFragment(): FollowingBattlesFeedFragment

    @ContributesAndroidInjector
    abstract fun contributeNotificationListFragment(): NotificationListFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchUsersAndBattlesFragment(): SearchUsersAndBattlesFragment

    @ContributesAndroidInjector
    abstract fun contributeUsersBattlesFragment(): UsersBattlesFragment

    @ContributesAndroidInjector
    abstract fun contributeUserSearchFragment(): UserSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeViewBattleFragment(): ViewBattleFragment

    @ContributesAndroidInjector
    abstract fun contributeViewCommentsFragment(): ViewCommentsFragment

    @ContributesAndroidInjector
    abstract fun contributeViewFollowingFragment(): ViewFollowingFragment

    @ContributesAndroidInjector
    abstract fun contributeChooseBattleTypeFragment(): ChooseBattleTypeFragment

    @ContributesAndroidInjector
    abstract fun contributeChooseOpponentFragment(): ChooseOpponentFragment

    @ContributesAndroidInjector
    abstract fun contributeVerifyBattleFragment(): VerifyBattleFragment

    @ContributesAndroidInjector
    abstract fun contributeAddFacebookFriendsAsFollowersStartupFragment(): AddFacebookFriendsAsFollowersStartupFragment

    @ContributesAndroidInjector
    abstract fun contributeChooseNameStartupFragment(): ChooseNameStartupFragment

    @ContributesAndroidInjector
    abstract fun contributeChooseUsernameStartupFragment(): ChooseUsernameStartupFragment

    @ContributesAndroidInjector
    abstract fun contributeLoggedInFragment(): LoggedInFragment

    @ContributesAndroidInjector
    abstract fun contributeChooseProfilePictureStartupFragment(): ChooseProfilePictureStartupFragment

    @ContributesAndroidInjector
    abstract fun contributeFullBattleVideoPlayerFragment(): FullBattleVideoPlayerFragment


}

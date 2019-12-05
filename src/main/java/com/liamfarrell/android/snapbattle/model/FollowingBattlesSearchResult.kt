package com.liamfarrell.android.snapbattle.model

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.db.FollowingBattle

/**
 * BattlesSearchResult from a load database, which contains LiveData<List<FollowingBattle>> holding query data,
 * and a LiveData<String> of network error state.
 */
data class FollowingBattlesSearchResult(
        val data: LiveData<PagedList<FollowingBattle>>,
        val networkErrors: LiveData<Throwable>
)

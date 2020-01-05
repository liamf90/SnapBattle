package com.liamfarrell.android.snapbattle.model

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.liamfarrell.android.snapbattle.db.AllBattlesBattle

/**
 * BattlesSearchResult from a load database, which contains LiveData<List<Battle>> holding query data,
 * and a LiveData<String> of network error state.
 */
data class BattlesSearchResult(
        val data: LiveData<PagedList<AllBattlesBattle>>,
        val networkErrors: LiveData<String>
)

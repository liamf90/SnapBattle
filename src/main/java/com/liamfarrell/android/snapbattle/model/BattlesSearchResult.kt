package com.liamfarrell.android.snapbattle.model

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

/**
 * BattlesSearchResult from a load database, which contains LiveData<List<Battle>> holding query data,
 * and a LiveData<String> of network error state.
 */
data class BattlesSearchResult(
        val data: LiveData<PagedList<Battle>>,
        val networkErrors: LiveData<String>
)

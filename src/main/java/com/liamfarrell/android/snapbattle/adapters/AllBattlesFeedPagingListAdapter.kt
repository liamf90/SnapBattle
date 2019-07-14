package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleFriendsBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.viewmodels.AllBattlesViewModel
import com.liamfarrell.android.snapbattle.viewmodels.BattleViewModel


/**
 * Adapter for the [RecyclerView] in [AllBattlesFragment].
 */
class AllBattlesFeedPagingListAdapter() :
        PagedListAdapter<Battle, AllBattlesFeedPagingListAdapter.ViewHolder>(BattleDiffCallback()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_battle_friends, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val battle = getItem(position)
        holder.apply {
            battle?.let { bind(it,
                    createChallengerOnClickListener(it.challengerCognitoID),
                    createChallengedOnClickListener(it.challengedCognitoID),
                    createThumbnailOnClickListener(it.battleID))
                itemView.tag = it}
        }
    }

    private fun createThumbnailOnClickListener(battleID: Int): View.OnClickListener {
        return View.OnClickListener {

        }
    }

    private fun createChallengerOnClickListener(challengerCognitoID: String): View.OnClickListener {
        return View.OnClickListener {

        }
    }

    private fun createChallengedOnClickListener(challengedCognitoID: String): View.OnClickListener {
        return View.OnClickListener {

        }
    }


    class ViewHolder(
            private val binding: ListItemBattleFriendsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Battle, challengerOnClick : View.OnClickListener, challengedOnClick : View.OnClickListener, thumbnailOnClick : View.OnClickListener) {
            with(binding) {
                battle = item
                viewModel = BattleViewModel(item)
                challengedUsernameClickListener = challengedOnClick
                challengerUsernameClickListener = challengerOnClick
                thumbnailClickListener = thumbnailOnClick
                executePendingBindings()
            }
        }
    }
}


private class BattleDiffCallback : DiffUtil.ItemCallback<Battle>() {

    override fun areItemsTheSame(oldItem: Battle, newItem: Battle): Boolean {
        return oldItem.battleID == newItem.battleID
    }

    override fun areContentsTheSame(oldItem: Battle, newItem: Battle): Boolean {
        return oldItem == newItem
    }
}


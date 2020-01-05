package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleFriendsBinding
import com.liamfarrell.android.snapbattle.db.FollowingBattle
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.mvvm_ui.HomeFragmentDirections
import com.liamfarrell.android.snapbattle.viewmodels.BattleViewModel


/**
 * Adapter for the [RecyclerView] in [FollowingBattlesFeedFragment].
 */
class FollowingBattlesFeedPagingListAdapter(val onBattleLoadedByAdapter : (b : Battle)->Unit) :
        PagedListAdapter<FollowingBattle, FollowingBattlesFeedPagingListAdapter.ViewHolder>(FollowingBattleDiffCallback()) {

    override fun getItemId(position: Int): Long {
        return super.getItem(position)?.followingBattleDb?.id?.toLong() ?: 0
    }

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
        battle?.let {onBattleLoadedByAdapter(it.followingBattleDb.battle) }
        holder.apply {
            battle?.let { bind(it.followingBattleDb.battle,
                    createChallengerOnClickListener(it.followingBattleDb.battle.challengerCognitoID),
                    createChallengedOnClickListener(it.followingBattleDb.battle.challengedCognitoID),
                    createThumbnailOnClickListener(it.followingBattleDb.battle),
                    it.lastSavedSignedUrl)
                itemView.tag = it}
        }
    }

    private fun createThumbnailOnClickListener(battle: Battle): View.OnClickListener {
        return View.OnClickListener {
            val direction = HomeFragmentDirections.actionNavigationHomeToNavigationFullBattleVideo(battle.battleId,  battle.getServerFinalVideoUrl(battle.challengerCognitoID), battle.challengerUsername, battle.challengedUsername)
            it.findNavController().navigate(direction)
        }
    }

    private fun createChallengerOnClickListener(challengerCognitoID: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = HomeFragmentDirections.actionNavigationHomeToNavigationUsersBattles(challengerCognitoID)
            it.findNavController().navigate(direction)
        }
    }

    private fun createChallengedOnClickListener(challengedCognitoID: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = HomeFragmentDirections.actionNavigationHomeToNavigationUsersBattles(challengedCognitoID)
            it.findNavController().navigate(direction)
        }
    }


    class ViewHolder(
            private val binding: ListItemBattleFriendsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Battle, challengerOnClick : View.OnClickListener, challengedOnClick : View.OnClickListener, thumbnailOnClick : View.OnClickListener, thumbSignedUrl : String?) {
            with(binding) {
                battle = item
                thumbnailSignedUrl = thumbSignedUrl
                viewModel = BattleViewModel(item)
                challengedUsernameClickListener = challengedOnClick
                challengerUsernameClickListener = challengerOnClick
                thumbnailClickListener = thumbnailOnClick
                executePendingBindings()
            }
        }
    }
}


private class FollowingBattleDiffCallback : DiffUtil.ItemCallback<FollowingBattle>() {

    override fun areItemsTheSame(oldItem: FollowingBattle, newItem: FollowingBattle): Boolean {
        return oldItem.followingBattleDb.id == newItem.followingBattleDb.id
    }

    override fun areContentsTheSame(oldItem: FollowingBattle, newItem: FollowingBattle): Boolean {
        return oldItem == newItem
    }
}


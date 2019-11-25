package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.client.AWSMobileClient
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleFriendsBinding
import com.liamfarrell.android.snapbattle.db.AllBattlesBattle
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.mvvm_ui.HomeFragmentDirections
import com.liamfarrell.android.snapbattle.viewmodels.AllBattlesViewModel
import com.liamfarrell.android.snapbattle.viewmodels.BattleViewModel




/**
 * Adapter for the [RecyclerView] in [AllBattlesFragment].
 */
class AllBattlesFeedPagingListAdapter(val onBattleLoadedByAdapter : (b : Battle)->Unit) :
        PagedListAdapter<AllBattlesBattle, AllBattlesFeedPagingListAdapter.ViewHolder>(BattleDiffCallback()) {

    override fun getItemId(position: Int): Long {
        return super.getItem(position)?.battle?.battleId?.toLong() ?: 0
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
        battle?.let {onBattleLoadedByAdapter(it.battle) }
        holder.apply {
            battle?.let { bind(it.battle,
                    createChallengerOnClickListener(it.battle.challengerCognitoID),
                    createChallengedOnClickListener(it.battle.challengedCognitoID),
                    createThumbnailOnClickListener(it.battle),
                    it.lastSavedSignedUrl)
                itemView.tag = it}
        }
    }

    private fun createThumbnailOnClickListener(battle: Battle): View.OnClickListener {
        return View.OnClickListener {
            val direction = HomeFragmentDirections.actionNavigationHomeToNavigationFullBattleVideo(battle.battleId, battle.getServerFinalVideoUrl(battle.challengerCognitoID) , battle.challengerUsername, battle.challengedUsername )
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

        fun bind(item: Battle, challengerOnClick : View.OnClickListener, challengedOnClick : View.OnClickListener, thumbnailOnClick : View.OnClickListener, thumbSignedUrl: String?) {
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


private class BattleDiffCallback : DiffUtil.ItemCallback<AllBattlesBattle>() {

    override fun areItemsTheSame(oldItem: AllBattlesBattle, newItem: AllBattlesBattle): Boolean {
        return oldItem.battle.battleId == newItem.battle.battleId
    }

    override fun areContentsTheSame(oldItem: AllBattlesBattle, newItem: AllBattlesBattle): Boolean {
        return oldItem == newItem
    }
}


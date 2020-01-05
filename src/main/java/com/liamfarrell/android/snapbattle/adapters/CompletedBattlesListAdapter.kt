package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.findNavController
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleFriendsBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.mvvm_ui.BattleCompletedListFragmentDirections
import com.liamfarrell.android.snapbattle.mvvm_ui.HomeFragmentDirections


/**
 * Adapter for the [RecyclerView] in [BattleCompletedListFragment].
 */
class CompletedBattlesListAdapter :
        ListAdapter<Battle, CompletedBattlesListAdapter.ViewHolder>(BattleDiffCallback()) {

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
            bind(battle, getOnClickListener(battle.battleId),
                    createChallengerOnClickListener(battle.challengerCognitoID),
                    createChallengedOnClickListener(battle.challengedCognitoID)
                    )
            itemView.tag = battle
        }
    }

    private fun getOnClickListener(battleID: Int): View.OnClickListener {
        return View.OnClickListener {
            //go to battle
            val action = BattleCompletedListFragmentDirections.actionBattleCompletedListFragmentToViewBattleFragment(battleID)
            it.findNavController().navigate(action)
        }
    }

    private fun createChallengerOnClickListener(challengerCognitoID: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = BattleCompletedListFragmentDirections.actionBattleCompletedListFragmentToNavigationUsersBattles(challengerCognitoID)
            it.findNavController().navigate(direction)
        }
    }

    private fun createChallengedOnClickListener(challengedCognitoID: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = BattleCompletedListFragmentDirections.actionBattleCompletedListFragmentToNavigationUsersBattles(challengedCognitoID)
            it.findNavController().navigate(direction)
        }
    }



    class ViewHolder(
            private val binding: ListItemBattleFriendsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Battle, thumbnailClickListener : View.OnClickListener,
                 challengerOnClick : View.OnClickListener, challengedOnClick : View.OnClickListener) {
            with(binding) {
                battle = item
                thumbnailSignedUrl = item.signedThumbnailUrl
                this.thumbnailClickListener  = thumbnailClickListener
                challengerUsernameClickListener =  challengerOnClick
                challengedUsernameClickListener =  challengedOnClick
                executePendingBindings()
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

}



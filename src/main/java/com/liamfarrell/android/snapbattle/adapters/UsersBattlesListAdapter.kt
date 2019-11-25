package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleFriendsBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.mvvm_ui.SearchUsersAndBattlesFragmentDirections
import com.liamfarrell.android.snapbattle.mvvm_ui.UsersBattlesFragmentDirections

/**
 * Adapter for the [RecyclerView] in [UsersBattlesFragment].
 */
class UsersBattlesListAdapter :
        ListAdapter<Battle, UsersBattlesListAdapter.ViewHolder>(BattleDiffCallback()) {

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
            bind(battle,
                    createChallengerOnClickListener(battle.challengerCognitoID),
                    createChallengedOnClickListener(battle.challengedCognitoID),
                    createThumbnailOnClickListener(battle))
            itemView.tag = battle
        }
    }



    private fun createThumbnailOnClickListener(battle: Battle): View.OnClickListener {
        return View.OnClickListener {
            val direction = UsersBattlesFragmentDirections.actionUsersBattlesFragment4ToNavigationFullBattleVideo(battle.battleId, battle.getServerFinalVideoUrl(battle.challengerCognitoID), battle.challengerUsername, battle.challengedUsername)
            it.findNavController().navigate(direction)
        }
    }

    private fun createChallengerOnClickListener(challengerCognitoID: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = UsersBattlesFragmentDirections.actionUsersBattlesFragment4Self(challengerCognitoID)
            it.findNavController().navigate(direction)
        }
    }

    private fun createChallengedOnClickListener(challengedCognitoID: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = UsersBattlesFragmentDirections.actionUsersBattlesFragment4Self(challengedCognitoID)
            it.findNavController().navigate(direction)
        }
    }



    class ViewHolder(
            private val binding: ListItemBattleFriendsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Battle, challengerOnClick : View.OnClickListener, challengedOnClick : View.OnClickListener, thumbnailOnClick : View.OnClickListener) {
            with(binding) {
                battle = item
                challengedUsernameClickListener = challengedOnClick
                challengerUsernameClickListener = challengerOnClick
                thumbnailClickListener = thumbnailOnClick
                thumbnailSignedUrl = item.signedThumbnailUrl
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
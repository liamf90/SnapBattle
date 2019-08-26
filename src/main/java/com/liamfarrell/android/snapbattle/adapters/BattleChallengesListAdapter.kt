package com.liamfarrell.android.snapbattle.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleChallengeBinding
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleFriendsBinding
import com.liamfarrell.android.snapbattle.model.Battle

/**
 * Adapter for the [RecyclerView] in [BattleChallengesListFragment].
 */

interface BattleChallengesAdapterCallbacks{
    fun onBattleAccepted(holder : BattleChallengesListAdapter.ViewHolder, battle: Battle)
    fun onBattleDeclined(battle: Battle)
}

class BattleChallengesListAdapter(val activity: BattleChallengesAdapterCallbacks) :
        ListAdapter<Battle, BattleChallengesListAdapter.ViewHolder>(BattleDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_battle_challenge, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val battle = getItem(position)
        holder.apply {
            bind(battle, getOnAcceptButtonClickListener(this, battle), getOnDeclineButtonOnClickListener(this, battle),
                    getProfilePicOnClickListener(battle.getOpponentCognitoID(IdentityManager.getDefaultIdentityManager().cachedUserID)))
            itemView.tag = battle
        }
    }

    private fun getOnAcceptButtonClickListener(holder : ViewHolder, battle: Battle): View.OnClickListener {
        return View.OnClickListener {
            holder.setAcceptButtonEnabled(false)
            holder.setDeclineButtonEnabled(false)
            activity.onBattleAccepted(holder, battle)
        }
    }

    private fun getOnDeclineButtonOnClickListener(holder : ViewHolder, battle: Battle): View.OnClickListener {
        return View.OnClickListener {
            holder.setAcceptButtonEnabled(false)
            holder.setDeclineButtonEnabled(false)
            activity.onBattleDeclined(battle)
        }
    }

    private fun getProfilePicOnClickListener(cognitoIdOpponent: String): View.OnClickListener {
        return View.OnClickListener {
            //go to user
        }
    }



    class ViewHolder(
            private val binding: ListItemBattleChallengeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Battle, acceptButtonOnClickListener: View.OnClickListener, declineButtonOnClickListener: View.OnClickListener,
                 profilePicOnClickListener: View.OnClickListener) {
            with(binding) {
                battle = item
                this.acceptButtonOnClickListener  = acceptButtonOnClickListener
                this.declineButtonOnClickListener = declineButtonOnClickListener
                this.profilePicOnClickListener = profilePicOnClickListener
                showAcceptButton = true
                showDeclineButton = true
                executePendingBindings()
            }
        }

        fun setAcceptButtonEnabled(enabled: Boolean){
            binding.showAcceptButton = enabled
            binding.executePendingBindings()
        }

        fun setDeclineButtonEnabled(enabled: Boolean){
            binding.showDeclineButton = enabled
            binding.executePendingBindings()
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
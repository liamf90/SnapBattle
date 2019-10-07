package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleChallengeBinding
import com.liamfarrell.android.snapbattle.model.Battle

/**
 * Adapter for the [RecyclerView] in [BattleChallengesListFragment].
 */

interface BattleChallengesAdapterCallbacks{
    fun onBattleAccepted(holder : BattleChallengesListAdapter.ViewHolder, battle: Battle)
    fun onBattleDeclined(battle: Battle)
}

class BattleChallengesListAdapter(val callbacks: BattleChallengesAdapterCallbacks) :
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
            callbacks.onBattleAccepted(holder, battle)
        }
    }

    private fun getOnDeclineButtonOnClickListener(holder : ViewHolder, battle: Battle): View.OnClickListener {
        return View.OnClickListener {
            holder.setAcceptButtonEnabled(false)
            holder.setDeclineButtonEnabled(false)
            callbacks.onBattleDeclined(battle)
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
                enableAcceptButton = true
                enableDeclineButton = true
                executePendingBindings()
            }
        }

        fun setAcceptButtonEnabled(enabled: Boolean){
            binding.enableAcceptButton = enabled
            binding.executePendingBindings()
        }

        fun setDeclineButtonEnabled(enabled: Boolean){
            binding.enableDeclineButton = enabled
            binding.executePendingBindings()
        }
    }

    private class BattleDiffCallback : DiffUtil.ItemCallback<Battle>() {

        override fun areItemsTheSame(oldItem: Battle, newItem: Battle): Boolean {
            return oldItem.battleId == newItem.battleId
        }

        override fun areContentsTheSame(oldItem: Battle, newItem: Battle): Boolean {
            return oldItem == newItem
        }
    }

}
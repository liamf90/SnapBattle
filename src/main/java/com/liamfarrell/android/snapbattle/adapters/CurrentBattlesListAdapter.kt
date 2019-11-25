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
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleCurrentBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.mvvm_ui.BattleCurrentListFragmentDirections


/**
 * Adapter for the [RecyclerView] in [BattleCurrentListFragment].
 */
class CurrentBattlesListAdapter(private val currentCognitoId: String) :
        ListAdapter<Battle, CurrentBattlesListAdapter.ViewHolder>(BattleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_battle_current, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val battle = getItem(position)
        holder.apply {
            bind(battle, currentCognitoId,  getProfilePicOnClickListener(battle.getOpponentCognitoID(currentCognitoId)))
            itemView.tag = battle
            holder.itemView.setOnClickListener(getOnClickListener(battle.battleID))
        }
    }

    private fun getOnClickListener(battleID: Int): View.OnClickListener {
        return View.OnClickListener {
            //go to battle
            val action = BattleCurrentListFragmentDirections.actionBattleCurrentListFragmentToViewBattleFragment(battleID)
            it.findNavController().navigate(action)
        }
    }

    private fun getProfilePicOnClickListener(cognitoIdOpponent: String): View.OnClickListener {
        return View.OnClickListener {
            //go to user
            val direction = BattleCurrentListFragmentDirections.actionBattleCurrentListFragmentToNavigationUsersBattles(cognitoIdOpponent)
            it.findNavController().navigate(direction)
        }
    }


    class ViewHolder(
            private val binding: ListItemBattleCurrentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Battle, currentCognitoId: String, profilePicOnClickListener: View.OnClickListener) {
            with(binding) {
                this.currentCognitoID = currentCognitoId
                this.profilePicOnClickListener = profilePicOnClickListener
                battle = item
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
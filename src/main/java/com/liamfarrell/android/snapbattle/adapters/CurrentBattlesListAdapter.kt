package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.databinding.ListItemCommentBinding
import com.liamfarrell.android.snapbattle.model.Comment
import androidx.appcompat.widget.PopupMenu
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleCurrentBinding
import com.liamfarrell.android.snapbattle.databinding.ListItemBattleFriendsBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModel
import kotlinx.android.synthetic.main.media_controller_battle.view.*


/**
 * Adapter for the [RecyclerView] in [BattleCompletedListFragment].
 */
class CurrentBattlesListAdapter(val opponentCognitoID: String) :
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
            bind(battle, opponentCognitoID)
            itemView.tag = battle
            holder.itemView.setOnClickListener(getOnClickListener(battle.battleID))
        }
    }

    private fun getOnClickListener(battleID: Int): View.OnClickListener {
        return View.OnClickListener {
            //go to battle
        }
    }



    class ViewHolder(
            private val binding: ListItemBattleCurrentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Battle, opponentCognitoID: String) {
            with(binding) {
                this.opponentCognitoID = opponentCognitoID
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
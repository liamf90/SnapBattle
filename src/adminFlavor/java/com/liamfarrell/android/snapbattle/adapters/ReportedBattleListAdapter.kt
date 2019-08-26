package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemReportedBattleBinding
import com.liamfarrell.android.snapbattle.model.ReportedBattle



/**
 * Adapter for the [RecyclerView] in [BattlesReportedFragment].
 */

interface ReportedBattleCallback {
    fun onIgnoreBattle(battleId: Int)
    fun onDeleteBattle(battleId: Int)
    fun onBanChallenger(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int)
    fun onBanChallenged(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int)
}

class ReportedBattleListAdapter(val callback : ReportedBattleCallback) :
        ListAdapter<ReportedBattle, ReportedBattleListAdapter.ViewHolder>(ReportedBattleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_reported_battle, parent, false
                ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reportedBattle = getItem(position)
        holder.apply {
            bind(reportedBattle, callback )
            itemView.tag = reportedBattle
        }
    }


    class ViewHolder(
            private val binding: ListItemReportedBattleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReportedBattle, callback : ReportedBattleCallback
                 ) {
            with(binding) {
                reportedBattle = item
                callbacks = callback
                executePendingBindings()
            }
        }
    }
}


private class ReportedBattleDiffCallback : DiffUtil.ItemCallback<ReportedBattle>() {

    override fun areItemsTheSame(oldItem: ReportedBattle, newItem: ReportedBattle): Boolean {
        return (oldItem.battleId != null && newItem.battleId != null && oldItem.battleId == newItem.battleId)
    }

    override fun areContentsTheSame(oldItem: ReportedBattle, newItem: ReportedBattle): Boolean {
        return oldItem == newItem
    }
}
package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemReportedBattleBinding
import com.liamfarrell.android.snapbattle.databinding.ListItemReportedCommentBinding
import com.liamfarrell.android.snapbattle.model.ReportedComment


/**
 * Adapter for the [RecyclerView] in [BattlesReportedFragment].
 */

interface ReportedCommentCallback {
    fun onIgnoreComment(commentId: Int)
    fun onDeleteComment(commentId: Int)
    fun onBanUser(commentId: Int, cognitoIdUserBan: String, banLengthDays: Int)
}

class ReportedCommentListAdapter(val callback : ReportedCommentCallback) :
        ListAdapter<ReportedComment, ReportedCommentListAdapter.ViewHolder>(ReportedCommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_reported_comment, parent, false
                ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reportedComment = getItem(position)
        holder.apply {
            bind(reportedComment, callback )
            itemView.tag = reportedComment
        }
    }


    class ViewHolder(
            private val binding: ListItemReportedCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReportedComment, callback : ReportedCommentCallback
        ) {
            with(binding) {
                reportedComment = item
                callbacks = callback
                executePendingBindings()
            }
        }
    }
}


private class ReportedCommentDiffCallback : DiffUtil.ItemCallback<ReportedComment>() {

    override fun areItemsTheSame(oldItem: ReportedComment, newItem: ReportedComment): Boolean {
        return (oldItem.commentId != null && newItem.commentId != null && oldItem.commentId == newItem.commentId)
    }

    override fun areContentsTheSame(oldItem: ReportedComment, newItem: ReportedComment): Boolean {
        return oldItem == newItem
    }
}
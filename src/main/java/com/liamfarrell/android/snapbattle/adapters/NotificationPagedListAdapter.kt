package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemNotificationBinding
import com.liamfarrell.android.snapbattle.notifications.Notification
import com.liamfarrell.android.snapbattle.notifications.NotificationDb

/**
 * Adapter for the [RecyclerView] in [NotificationListFragment].
 */
class NotificationPagedListAdapter :
        PagedListAdapter<NotificationDb, NotificationPagedListAdapter.ViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_notification, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = getItem(position)
        holder.apply {
            notification?.let {
                bind(it)
                itemView.tag = it
            }
        }
    }


    class ViewHolder(
            private val binding: ListItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationDb) {
            with(binding) {
                notification = item.getNotification()
                executePendingBindings()
            }
        }
    }
}


private class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationDb>() {

    override fun areItemsTheSame(oldItem: NotificationDb, newItem: NotificationDb): Boolean {
        return oldItem.notificationIndex == newItem.notificationIndex
    }

    override fun areContentsTheSame(oldItem: NotificationDb, newItem: NotificationDb): Boolean {
        return oldItem == newItem
    }
}
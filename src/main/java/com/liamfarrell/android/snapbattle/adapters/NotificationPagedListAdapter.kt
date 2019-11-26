package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemNotificationBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Video
import com.liamfarrell.android.snapbattle.mvvm_ui.NotificationListFragmentDirections
import com.liamfarrell.android.snapbattle.notifications.*
import java.lang.IllegalArgumentException

/**
 * Adapter for the [RecyclerView] in [NotificationListFragment].
 */
class NotificationPagedListAdapter(val onNotificationLoadedByAdapter : (n : Notification)->Unit) :
        PagedListAdapter<NotificationDb, NotificationPagedListAdapter.ViewHolder>(NotificationDiffCallback()) {

    override fun getItemId(position: Int): Long {
        return super.getItem(position)?.notificationIndex?.toLong() ?: 0
    }


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
        notification?.let{onNotificationLoadedByAdapter(it.getNotification())}
        holder.apply {
            notification?.let {
                bind(it)
                itemView.tag = it
                itemView.setOnClickListener(getItemOnClickListener(it.getNotification()))
            }
        }
    }

    private fun getItemOnClickListener(notification: Notification) : View.OnClickListener{
        return View.OnClickListener {
            val direction = when(notification) {
                is BattleAcceptedNotification ->  NotificationListFragmentDirections.actionNotificationListFragmentToViewBattleFragment(notification.battleId)
                is FullVideoUploadedNotification -> NotificationListFragmentDirections.actionNotificationListFragmentToViewBattleFragment(notification.battleId)
                is NewBattleRequestNotification -> NotificationListFragmentDirections.actionNotificationListFragmentToBattleChallengesListFragment2()
                is NewCommentNotification -> NotificationListFragmentDirections.actionNotificationListFragmentToViewBattleFragment(notification.battleId)
                is NewFollowerNotification -> NotificationListFragmentDirections.actionNotificationListFragmentToViewFollowingFragment2()
                is TaggedInCommentNotification -> NotificationListFragmentDirections.actionNotificationListFragmentToNavigationFullBattleVideo(notification.battleId, Battle.getServerFinalVideoUrlStatic(notification.challengerCognitoId, notification.battleId), notification.challengerUsername, notification.challengedUsername)
                is VideoSubmittedNotification -> NotificationListFragmentDirections.actionNotificationListFragmentToViewBattleFragment(notification.battleId)
                is VotingCompleteNotification -> NotificationListFragmentDirections.actionNotificationListFragmentToViewBattleFragment(notification.battleId)
                else -> throw IllegalArgumentException("Notification type on click not specified")
            }
            it.findNavController().navigate(direction)
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
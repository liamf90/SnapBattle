package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemFollowingBinding
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.viewmodels.FollowFacebookFriendsViewModel


/**
 * Adapter for the [RecyclerView] in [FollowFacebookFriendsFragment].
 */
class FollowFacebookFriendsListAdapter(val viewModel: FollowFacebookFriendsViewModel) :
        ListAdapter<User, FollowFacebookFriendsListAdapter.ViewHolder>(UserFacebookFriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_facebook_friends, parent, false
                ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.apply {
            bind(user, createOnProfilePicClickListener(), createOnModifyClickListener(user))
            itemView.tag = user
        }
    }

    private fun createOnProfilePicClickListener(): View.OnClickListener {
        return View.OnClickListener {
            //TODO:  GO TO PROFILE FRAGMENT
        }
    }

    private fun createOnModifyClickListener(user: User): View.OnClickListener {
        return View.OnClickListener {
            if (user.isFollowing) viewModel.removeFollowing(user.cognitoId) else viewModel.addFollowing(user.username)
        }
    }

    class ViewHolder(
            private val binding: ListItemFollowingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User, profilePicClicklistener: View.OnClickListener, modifyUserClickListener : View.OnClickListener) {
            with(binding) {
                user = item
                onProfilePictureClickListener = profilePicClicklistener
                onModifyClickListener = modifyUserClickListener
                executePendingBindings()
            }
        }
    }
}


class UserFacebookFriendDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.facebookUserId == newItem.facebookUserId
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
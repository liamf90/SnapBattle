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
import com.liamfarrell.android.snapbattle.databinding.ListItemFacebookFriendsBinding
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.mvvm_ui.FollowFacebookFriendsFragmentDirections


/**
 * Adapter for the [RecyclerView] in [FollowFacebookFriendsFragment].
 */
class FollowFacebookFriendsListAdapter(val addFollowingCallback : (facebookId: String)->Unit, val removeFollowingCallback : (facebookId: String)->Unit) :
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
            bind(user, createOnProfilePicClickListener(user.facebookUserId), createOnModifyClickListener(user))
            itemView.tag = user
        }
    }

    private fun createOnProfilePicClickListener(facebookUserId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = FollowFacebookFriendsFragmentDirections.actionFollowFacebookFriendsFragmentToNavigationUsersBattles2(facebookUserId)
            it.findNavController().navigate(direction)
        }
    }

    private fun createOnModifyClickListener(user: User): View.OnClickListener {
        return View.OnClickListener {
            if (user.isFollowing){
                removeFollowingCallback(user.cognitoId)
            } else {
                addFollowingCallback(user.facebookUserId)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemFacebookFriendsBinding
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
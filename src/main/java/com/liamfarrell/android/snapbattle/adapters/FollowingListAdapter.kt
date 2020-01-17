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
import com.liamfarrell.android.snapbattle.databinding.ListItemFollowingBinding
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.mvvm_ui.ViewFollowingFragmentDirections


/**
 * Adapter for the [RecyclerView] in [ViewFollowingFragment].
 */
class FollowingListAdapter(val removeFollowing : (cognitoId: String)->Unit, val followUser : (cognitoId: String)->Unit) :
        ListAdapter<User, FollowingListAdapter.ViewHolder>(UserDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_following, parent, false
                ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.apply {
            bind(user, createOnProfilePicClickListener(user.cognitoId), createOnModifyClickListener(user))
            itemView.tag = user
        }
    }

    private fun createOnProfilePicClickListener(cognitoId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = ViewFollowingFragmentDirections.actionViewFollowingFragmentToNavigationUsersBattles(cognitoId)
            it.findNavController().navigate(direction)
        }
    }

    private fun createOnModifyClickListener(user: User): View.OnClickListener {
        return View.OnClickListener {
           if (user.isFollowing){
               removeFollowing(user.cognitoId)
           } else {
               followUser(user.cognitoId)
           }
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


class UserDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.cognitoId == newItem.cognitoId
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
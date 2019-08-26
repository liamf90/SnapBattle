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
import com.liamfarrell.android.snapbattle.databinding.ListItemOpponentSelectBinding
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.viewmodels.FollowFacebookFriendsViewModel


/**
 * Adapter for the [RecyclerView] in [ChooseOpponentFragment].
 */
class ChooseOpponentListAdapter(val opponentSelectedCallback : (user: User) -> Unit) :
        ListAdapter<User, ChooseOpponentListAdapter.ViewHolder>(UserOpponentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_opponent_select, parent, false
                ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.apply {
            bind(user, createOnProfilePicClickListener())
            itemView.tag = user
        }
    }

    private fun createOnProfilePicClickListener(): View.OnClickListener {
        return View.OnClickListener {
            //TODO:  GO TO PROFILE FRAGMENT
        }
    }

    private fun createOnItemClickListener(user: User) : View.OnClickListener {
        return View.OnClickListener {
            //Set the opponent
            opponentSelectedCallback(user)
        }
    }

    class ViewHolder(
            private val binding: ListItemOpponentSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User, profilePicClicklistener: View.OnClickListener) {
            with(binding) {
                user = item
                executePendingBindings()
            }
        }
    }
}


private class UserOpponentDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return (oldItem.cognitoId != null && newItem.cognitoId != null && oldItem.cognitoId == newItem.cognitoId) ||
                (oldItem.facebookUserId != null && newItem.facebookUserId != null && oldItem.facebookUserId == newItem.facebookUserId)
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
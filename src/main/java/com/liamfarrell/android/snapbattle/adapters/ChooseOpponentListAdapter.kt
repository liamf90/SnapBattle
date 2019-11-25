package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemOpponentSelectBinding
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseOpponentFragmentDirections


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
            if (user.cognitoId != null){
                bind(user, createOnProfilePicClickListener(user.cognitoId), createOnItemClickListener(user))
            } else {
                bind(user, createOnProfilePicClickListenerFacebookFriend(user.facebookUserId), createOnItemClickListener(user))
            }
            itemView.tag = user
        }
    }

    private fun createOnProfilePicClickListener(cognitoId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = ChooseOpponentFragmentDirections.actionChooseOpponentFragmentToNavigationUsersBattles(cognitoId)
            it.findNavController().navigate(direction)
        }
    }

    private fun createOnProfilePicClickListenerFacebookFriend(facebookId: String): View.OnClickListener {
        return View.OnClickListener {
            val direction = ChooseOpponentFragmentDirections.actionChooseOpponentFragmentToNavigationUsersBattles2(facebookId)
            it.findNavController().navigate(direction)
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
        fun bind(item: User, profilePicClicklistener: View.OnClickListener, itemClickListener: View.OnClickListener) {
            with(binding) {
                user = item
                onItemClickListener = itemClickListener
                onProfilePictureClickListener = profilePicClicklistener
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
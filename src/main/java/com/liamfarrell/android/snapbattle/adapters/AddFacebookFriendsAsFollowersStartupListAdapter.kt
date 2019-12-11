package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemFacebookFriendsSelectBinding
import com.liamfarrell.android.snapbattle.model.User


/**
 * Adapter for the [RecyclerView] in [AddFacebookFriendsAsFollowersStartupFragment].
 */
class AddFacebookFriendsAsFollowersStartupListAdapter(val onCheckedChanged: (enableNextButton: Boolean)-> Unit) :
        ListAdapter<User, AddFacebookFriendsAsFollowersStartupListAdapter.ViewHolder>(UserFacebookFriendDiffCallback()) {
    var checkedCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_facebook_friends_select, parent, false
                ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.apply {
            bind(user, getOnCheckboxChangedListener())
            itemView.tag = user

        }
    }

    private fun getOnCheckboxChangedListener() : CompoundButton.OnCheckedChangeListener {
        return CompoundButton.OnCheckedChangeListener{ _: CompoundButton, b: Boolean ->
            val checkedInitial = checkedCount
            if (b) checkedCount ++
            else checkedCount --

            if (checkedInitial == 0 && checkedCount != 0){
                onCheckedChanged(true)
            } else if (checkedInitial != 0 && checkedCount == 0){
                onCheckedChanged(false)
            }

        }
    }



    class ViewHolder(
            private val binding: ListItemFacebookFriendsSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User, OnCheckedChangedListener : CompoundButton.OnCheckedChangeListener ) {
            with(binding) {
                user = item
                onCheckedChangedListener = OnCheckedChangedListener
                executePendingBindings()
            }
        }
    }
}

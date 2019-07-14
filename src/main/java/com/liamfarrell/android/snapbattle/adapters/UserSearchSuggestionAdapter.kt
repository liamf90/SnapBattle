package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.ListItemSearchBattleBinding
import com.liamfarrell.android.snapbattle.databinding.ListItemSearchUserBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse
import com.liamfarrell.android.snapbattle.ui.UserSearchFragment

/**
 * Adapter for the [RecyclerView] in [BattleNameSearchFragment].
 */
class UserSearchSuggestionAdapter :
        ListAdapter<User, RecyclerView.ViewHolder>(UserDiffCallback()) {

    var state : UserSearchFragment.State = UserSearchFragment.State.SHOW_LIST




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.list_item_search_battle -> {
                return ViewHolder(DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_search_user, parent, false
                ))
            }
            R.layout.list_item_no_results -> {
                return LoadingViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_no_results, parent, false)
                )


            }
            R.layout.list_item_loading -> {
                return LoadingViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_loading, parent, false)
                )

            }
            else -> {
                return ViewHolder(DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_search_battle, parent, false
                ))

            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        when (state) {
            UserSearchFragment.State.SHOW_LIST -> {
                return R.layout.list_item_search_user
            }
            UserSearchFragment.State.NO_RESULTS -> {
                return R.layout.list_item_no_results
            }
            UserSearchFragment.State.LOADING -> {
                return R.layout.list_item_loading
            }
            else -> return R.layout.list_item_search_battle
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val user = getItem(position)
            holder.apply {
                bind(user)
                holder.itemView.setOnClickListener(getOnClickListener(user.cognitoId))
            }
        }
    }

    override fun getItemCount(): Int {
        if (state != UserSearchFragment.State.SHOW_LIST) return 1

        return super.getItemCount()
    }

    private fun getOnClickListener(cognitoID: String): View.OnClickListener {
        return View.OnClickListener {
            //go to user
        }
    }



    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view){
    }

    class ViewHolder(
            private val binding: ListItemSearchUserBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: User) {
            with(binding) {
                user = item
                executePendingBindings()
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.cognitoId == newItem.cognitoId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

}
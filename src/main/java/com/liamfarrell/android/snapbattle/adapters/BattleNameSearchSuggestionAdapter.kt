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
import com.liamfarrell.android.snapbattle.databinding.ListItemSearchBattleBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.SearchUsersAndBattlesFragmentDirections
import com.liamfarrell.android.snapbattle.ui.UserSearchFragment

/**
 * Adapter for the [RecyclerView] in [BattleNameSearchFragment].
 */
class BattleNameSearchSuggestionAdapter :
        ListAdapter<SuggestionsResponse, RecyclerView.ViewHolder>(SuggestionsResponseDiffCallback()) {

    var state : UserSearchFragment.State = UserSearchFragment.State.SHOW_LIST




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.list_item_search_battle -> {
                return ViewHolder(DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_search_battle, parent, false
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
                return R.layout.list_item_search_battle
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
            val suggestionsResponse = getItem(position)
            holder.apply {
                bind(suggestionsResponse)
                holder.itemView.setOnClickListener(getOnClickListener(suggestionsResponse.battleName))
            }
        }
    }

    override fun getItemCount(): Int {
        if (state != UserSearchFragment.State.SHOW_LIST) return 1

        return super.getItemCount()
    }

    private fun getOnClickListener(battleName: String): View.OnClickListener {
        return View.OnClickListener {
            //go to battle
            val direction = SearchUsersAndBattlesFragmentDirections.actionSearchUsersAndBattlesFragmentToViewBattlesFromNameFragment(battleName)
            it.findNavController().navigate(direction)
        }
    }



    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view){
    }

    class ViewHolder(
            private val binding: ListItemSearchBattleBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SuggestionsResponse) {
            with(binding) {
                suggestion = item
                executePendingBindings()
            }
        }
    }

    private class SuggestionsResponseDiffCallback : DiffUtil.ItemCallback<SuggestionsResponse>() {

        override fun areItemsTheSame(oldItem: SuggestionsResponse, newItem: SuggestionsResponse): Boolean {
            return oldItem.battleName == newItem.battleName
        }

        override fun areContentsTheSame(oldItem: SuggestionsResponse, newItem: SuggestionsResponse): Boolean {
            return oldItem == newItem
        }
    }

}
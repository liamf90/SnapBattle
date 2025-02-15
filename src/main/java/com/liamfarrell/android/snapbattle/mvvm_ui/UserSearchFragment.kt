package com.liamfarrell.android.snapbattle.mvvm_ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.adapters.UserSearchSuggestionAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentUserSearchBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.viewmodels.UserSearchViewModel
import javax.inject.Inject

class UserSearchFragment : Fragment(), Injectable {

    enum class State {
        LOADING,
        NO_RESULTS,
        SHOW_LIST
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: UserSearchViewModel
    private val adapter = UserSearchSuggestionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentUserSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel = ViewModelProvider(this, viewModelFactory).get(UserSearchViewModel::class.java)
        binding.recyclerList.adapter = adapter
        binding.recyclerList.setItemAnimator(null);
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.searchList.observe(viewLifecycleOwner, Observer { searchResult ->
            adapter.submitList(searchResult)
            adapter.notifyDataSetChanged()
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.searchState.observe(viewLifecycleOwner, Observer {
           adapter.state = it
           adapter.notifyDataSetChanged()
        })
    }

    @SuppressLint("CheckResult")
    fun setOnQueryChangedListener(searchView: SearchView){
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == null){
                viewModel.searchUserSubmit("")}
                else {
                    viewModel.searchUserSubmit(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
            if (newText == null){
                viewModel.searchQueryChange("")
            }
            else {
                viewModel.searchQueryChange(newText)
            }
                return true
            }
        })
    }



}
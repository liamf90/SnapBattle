package com.liamfarrell.android.snapbattle.mvvm_ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.adapters.BattleNameSearchSuggestionAdapter
import com.liamfarrell.android.snapbattle.adapters.UserSearchSuggestionAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentBattleNameSearchBinding
import com.liamfarrell.android.snapbattle.databinding.FragmentUserSearchBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.ui.UserSearchFragment
import com.liamfarrell.android.snapbattle.viewmodels.UserSearchViewModel
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import java.util.concurrent.TimeUnit

class UserSearchFragment : Fragment(){


    private val TAG = "UserSearchFragment"

    private lateinit var viewModel: UserSearchViewModel
    private val adapter = UserSearchSuggestionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentUserSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val appComponent = DaggerUserSearchComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .userSearchViewModelFactoryModule(UserSearchViewModelFactoryModule())
                .userSearchRepositoryModule(UserSearchRepositoryModule(requireContext()))
                .build()


        viewModel = ViewModelProviders.of(this, appComponent.getUserSearchViewModelFactory()).get(UserSearchViewModel::class.java)
        binding.recyclerList.adapter = adapter
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.searchResult.observe(viewLifecycleOwner, Observer { searchResult ->
            if (searchResult.isEmpty()){
                adapter.state = UserSearchFragment.State.NO_RESULTS
            }
            else {
                adapter.state = UserSearchFragment.State.SHOW_LIST
            }
            adapter.submitList(searchResult)
            adapter.notifyDataSetChanged()
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.spinner.observe(viewLifecycleOwner, Observer {
            if (it){
                adapter.state = UserSearchFragment.State.LOADING
                adapter.submitList(mutableListOf())
                adapter.notifyDataSetChanged()
            }
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
                viewModel.searchUserSubmit("")
            }
            else {
                viewModel.searchUserSubmit(newText)
            }
                return true
            }
        })
    }



}
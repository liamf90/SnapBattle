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
import com.liamfarrell.android.snapbattle.databinding.FragmentBattleNameSearchBinding
import com.liamfarrell.android.snapbattle.di.AWSLambdaModule
import com.liamfarrell.android.snapbattle.di.BattleNameSearchViewModelFactoryModule
import com.liamfarrell.android.snapbattle.di.DaggerBattleNameSearchComponent
import com.liamfarrell.android.snapbattle.ui.UserSearchFragment
import com.liamfarrell.android.snapbattle.viewmodels.BattleNameSearchViewModel
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import java.util.concurrent.TimeUnit

class BattleNameSearchFragment : Fragment(){


    private val TAG = "BattleNameSearchFrag"

    private lateinit var viewModel: BattleNameSearchViewModel
    private val adapter = BattleNameSearchSuggestionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentBattleNameSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val appComponent = DaggerBattleNameSearchComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .battleNameSearchViewModelFactoryModule(BattleNameSearchViewModelFactoryModule())
                .build()


        viewModel = ViewModelProviders.of(this, appComponent.getBattleNameSearchViewModelFactory()).get(BattleNameSearchViewModel::class.java)
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
        // Set up the query listener that executes the search
        Observable.create(ObservableOnSubscribe<String> { subscriber ->
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    subscriber.onNext(newText!!)
                    Log.i(TAG, "On next: " + newText)
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    subscriber.onNext(query!!)
                    Log.i(TAG, "On submit: " + query)
                    return false
                }
            })
        })
        .map { text -> text.toLowerCase().trim() }
        .debounce(250, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .filter { text -> text.isNotBlank() }
        .subscribe { text ->
            Log.i(TAG, "Sent: " + text)
            viewModel.searchBattle(text)
        }
    }



}
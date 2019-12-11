package com.liamfarrell.android.snapbattle.mvvm_ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.adapters.BattleNameSearchSuggestionAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentBattleNameSearchBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.viewmodels.BattleNameSearchViewModel
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BattleNameSearchFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BattleNameSearchViewModel
    private val adapter = BattleNameSearchSuggestionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentBattleNameSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this, viewModelFactory).get(BattleNameSearchViewModel::class.java)
        binding.recyclerList.adapter = adapter
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.searchResult.observe(viewLifecycleOwner, Observer { searchResult ->
            when {
                searchResult == null -> adapter.state = UserSearchFragment.State.SHOW_LIST
                searchResult.isEmpty() -> adapter.state = UserSearchFragment.State.NO_RESULTS
                else -> adapter.state = UserSearchFragment.State.SHOW_LIST
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
                    viewModel.setSearchQueryText(newText)

                    if (newText == null || newText  == "") {
                        viewModel.searchBattle("")
                    } else {
                        subscriber.onNext(newText)
                    }
                    Timber.i("On next: %s", newText)
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    subscriber.onNext(query!!)
                    Timber.i("On submit %s", query)
                    return false
                }
            })
        })
        .map { text -> text.toLowerCase().trim() }
        .debounce(250, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .filter { text -> text.isNotBlank() }
        .subscribe { text ->
            Timber.i("Sent: %s", text)
            viewModel.searchBattle(text)
        }
    }



}
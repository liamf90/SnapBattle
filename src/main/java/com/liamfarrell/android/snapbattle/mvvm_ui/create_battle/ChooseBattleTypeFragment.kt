package com.liamfarrell.android.snapbattle.mvvm_ui.create_battle

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.BattleNameSuggestionsAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentChooseBattleTypeBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.viewmodels.create_battle.ChooseBattleTypeViewModel
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import kotlinx.android.synthetic.main.fragment_choose_battle_type.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChooseBattleTypeFragment : Fragment() , Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ChooseBattleTypeViewModel
    private var enableNextButton = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentChooseBattleTypeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChooseBattleTypeViewModel::class.java)
        val adapter = BattleNameSuggestionsAdapter(requireContext(), mutableListOf())
        binding.onSuggestionTextClick = View.OnClickListener {view->   viewModel.battleName.value = (view as TextView).text.toString()
            battleTypeEditText.setSelection(battleTypeEditText.text.length)}
        binding.battleTypeEditText.setAdapter(adapter)
        binding.battleTypeEditText.threshold = 2
        binding.viewModel = viewModel
        setOnQueryChangedListener(binding.battleTypeEditText)
        subscribeUi(adapter)
        setHasOptionsMenu(true)
        setToolbar(binding.includeToolbar.toolbar)
        return binding.root
    }

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Create Battle";
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false);
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater : MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_create_battle, menu);
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        val id = item.itemId
        if (id == R.id.action_next) {
            val bundle = bundleOf("battleName" to viewModel.battleName.value)
            findNavController().navigate(R.id.action_chooseBattleTypeFragment_to_chooseOpponentFragment, bundle)
        }
        return false
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_next).isEnabled = enableNextButton
    }


    @SuppressLint("RestrictedApi")
    private fun subscribeUi(adapter: BattleNameSuggestionsAdapter) {
        viewModel.battleNameSearchList.observe(viewLifecycleOwner, Observer { battleNameList ->
            adapter.battleNameList = battleNameList
            adapter.notifyDataSetChanged()
        })

        viewModel.battleName.observe(viewLifecycleOwner, Observer { battleName ->
            if (battleName.isNotBlank()){
                if (!enableNextButton) {
                    enableNextButton = true
                    (activity as AppCompatActivity).supportActionBar?.invalidateOptionsMenu()
                }
            } else {
                if (enableNextButton){
                    enableNextButton = false
                    (activity as AppCompatActivity).supportActionBar?.invalidateOptionsMenu()
                }
            }
        })
    }


        @SuppressLint("CheckResult")
    fun setOnQueryChangedListener(autoCompleteTextView: AppCompatAutoCompleteTextView){
        // Set up the query listener that executes the search
        Observable.create(ObservableOnSubscribe<String> { subscriber ->
            autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    //Only allow user to go to next fragment if the battle name text is not empty
                    if (s.toString() != "") {
                        enableNextButton = true
                        activity?.invalidateOptionsMenu()
                    } else {
                        enableNextButton = false
                        activity?.invalidateOptionsMenu()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    subscriber.onNext(s.toString())
                }
            })

             })
            .map { text -> text.toLowerCase().trim() }
            .debounce(250, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .filter { text -> text.isNotBlank() }
            .subscribe { text ->
                viewModel.doBattleNameSuggestionSearch(text)
            }
    }

}
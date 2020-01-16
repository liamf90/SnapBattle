package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.CompletedBattlesListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentCompletedBattleListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.viewmodels.CompletedBattlesViewModel
import javax.inject.Inject

class BattleCompletedListFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CompletedBattlesViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCompletedBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this, viewModelFactory).get(CompletedBattlesViewModel::class.java)
        val adapter = CompletedBattlesListAdapter()
        binding.recyclerView.adapter = adapter
        binding.showSpinner = viewModel.spinner
        setToolbar(binding.includeToolbar.toolbar,  context?.getString(R.string.nav_completed_battles) ?: "")
        subscribeUi(binding, adapter)
        return binding.root
    }

    private fun subscribeUi(binding : FragmentCompletedBattleListBinding, adapter: CompletedBattlesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            battlesList?.let{
                adapter.submitList(battlesList)
                if (it.isEmpty()) binding.NoBattlesTextView.visibility = View.VISIBLE
                else binding.NoBattlesTextView.visibility = View.GONE
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar, title: String){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = title
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

}
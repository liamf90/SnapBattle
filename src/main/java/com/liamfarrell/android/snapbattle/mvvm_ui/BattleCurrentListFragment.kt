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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.CurrentBattlesListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentCurrentBattleListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.viewmodels.CurrentBattlesViewModel
import javax.inject.Inject

@OpenForTesting
class BattleCurrentListFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    
    private lateinit var viewModel: CurrentBattlesViewModel

    lateinit var binding : FragmentCurrentBattleListBinding

    @Inject
    lateinit var awsMobileClient: AWSMobileClient


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCurrentBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this, viewModelFactory).get(CurrentBattlesViewModel::class.java)
        val adapter = CurrentBattlesListAdapter(getCognitoId())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.showSpinner = viewModel.spinner
        setToolbar(binding.includeToolbar.toolbar,  context?.getString(R.string.nav_current_battles) ?: "")
        subscribeUi(binding, adapter)
        return binding.root
    }

    private fun subscribeUi(binding : FragmentCurrentBattleListBinding, adapter: CurrentBattlesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            battlesList?.let{
                adapter.submitList(battlesList)

                if (it.isEmpty()) binding.NoBattlesTextView.visibility = View.VISIBLE
                else binding.NoBattlesTextView.visibility = View.GONE
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar, title: String){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = title
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    fun getCognitoId() : String {
        return awsMobileClient.identityId
    }



    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()


}
package com.liamfarrell.android.snapbattle.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.adapters.ReportedBattleCallback
import com.liamfarrell.android.snapbattle.adapters.ReportedBattleListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentReportingsBinding
import com.liamfarrell.android.snapbattle.di.AWSLambdaModule
import com.liamfarrell.android.snapbattle.di.DaggerBattlesReportedComponent
import com.liamfarrell.android.snapbattle.viewmodel.BattlesReportedViewModel

class BattlesReportedFragment : Fragment(), ReportedBattleCallback {
    private lateinit var viewModel: BattlesReportedViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentReportingsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val appComponent = DaggerBattlesReportedComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .build()



        viewModel = ViewModelProviders.of(this, appComponent.getBattlesReportedViewModelFactory()).get(BattlesReportedViewModel::class.java)
        val adapter = ReportedBattleListAdapter(this)
        binding.recyclerList.adapter = adapter
        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: ReportedBattleListAdapter) {
        viewModel.reportedBattles.observe(viewLifecycleOwner, Observer { reportedBattlesList ->
            adapter.submitList(reportedBattlesList)
        })
    }

    override fun onIgnoreBattle(battleId: Int) {
        viewModel.ignoreBattle(battleId)
    }

    override fun onDeleteBattle(battleId: Int) {
        viewModel.deleteBattle(battleId)
    }

    override fun onBanChallenger(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int) {
        viewModel.banChallenger(battleId, cognitoIdUserBan, banLengthDays)
    }

    override fun onBanChallenged(battleId: Int, cognitoIdUserBan: String, banLengthDays: Int) {
        viewModel.banChallenged(battleId, cognitoIdUserBan, banLengthDays)
    }




}
package com.liamfarrell.android.snapbattle.mvvm_ui.create_battle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.databinding.FragmentCreateBattleBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Voting
import com.liamfarrell.android.snapbattle.viewmodels.VerifyBattleViewModel
import javax.inject.Inject

class VerifyBattleFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: VerifyBattleViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //get battle details
        val battleName = arguments?.getString("battleName")
        val rounds = arguments?.getInt("rounds") ?: 1
        val challengedCognitoId =  arguments?.getString("opponentCognitoId")
        val challengedFacebookId =  arguments?.getString("opponentFacebookId")
        val challengedName =  arguments?.getString("opponentName")
        val challengedUsername =  arguments?.getString("opponentUsername")
        val battle = Battle(-1, null, challengedCognitoId, battleName, rounds )
        battle.challengedFacebookUserId = challengedFacebookId
        battle.challengedName = challengedName
        battle.challengedUsername = challengedUsername

        val votingChoice  = ChooseVotingFragment.VotingChoice.valueOf(arguments?.getString("votingType") ?: ChooseVotingFragment.VotingChoice.NONE.toString())
        val votingLength = ChooseVotingFragment.VotingLength.valueOf(arguments?.getString("votingLength") ?: ChooseVotingFragment.VotingLength.TWENTY_FOUR_HOURS.toString())
        val voting = Voting(votingChoice, votingLength, null, null, null)
        battle.voting = voting

        val binding = FragmentCreateBattleBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VerifyBattleViewModel::class.java)
        viewModel.setBattle(battle)
        binding.viewModel = viewModel
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        })
    }


}
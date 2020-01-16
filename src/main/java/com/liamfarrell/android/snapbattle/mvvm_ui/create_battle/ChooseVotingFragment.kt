package com.liamfarrell.android.snapbattle.mvvm_ui.create_battle

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.VotingLengthAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentChooseVotingBinding
import kotlinx.android.synthetic.main.fragment_choose_voting.*

class ChooseVotingFragment : Fragment(), View.OnClickListener{

    private var chosenVotingChoice: VotingChoice? = VotingChoice.NONE


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceBundle: Bundle?): View? {
        val binding = FragmentChooseVotingBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.noVotingButton.setOnClickListener(this)
        binding.publicVotingButton.setOnClickListener(this)
        binding.mutualFacebookFriendsButton.setOnClickListener(this)
        binding.votingTypeRadioGroup.check(R.id.noVotingButton)

        // Apply the adapter to the spinner
        binding.votingLengthSpinner.adapter = VotingLengthAdapter(requireContext())
        binding.votingLengthSpinner.isEnabled = false
        binding.votingLengthSpinner.setSelection(0)
        setToolbar(binding.includeToolbar.toolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Choose Voting";
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false);
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater : MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_create_battle, menu);
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        val id = item.itemId
        if (id == R.id.action_next) {
            val bundle = bundleOf(("votingLength" to votingLengthSpinner.selectedItem.toString()),
                    ("votingType" to chosenVotingChoice.toString()))
            bundle.putAll(arguments)
            findNavController().navigate(R.id.action_chooseVotingFragment_to_verifyBattleFragment, bundle)
        }
        return false
    }

    override fun onClick(view: View) {
        // determine which voting option is checked and update the member variables
        val checked = (view as RadioButton).isChecked
        // Check which radio button was clicked
        when (view.getId()) {
            R.id.noVotingButton -> if (checked) {
                votingLengthSpinner.isEnabled = false
                chosenVotingChoice = VotingChoice.NONE
            }
            R.id.publicVotingButton -> if (checked) {
                votingLengthSpinner.isEnabled = true
                chosenVotingChoice = VotingChoice.PUBLIC
            }
            R.id.mutualFacebookFriendsButton -> if (checked) {
                votingLengthSpinner.isEnabled = true
                chosenVotingChoice = VotingChoice.MUTUAL_FACEBOOK
            }
        }
    }


    enum class VotingChoice {

        MUTUAL_FACEBOOK {
            override fun toLongStyleString(context: Context): String {
                return context.resources.getText(R.string.mutual_facebook_friends_long).toString()
            }

            override fun toString(context: Context): String {
                return context.resources.getText(R.string.mutual_facebook_friends).toString()
            }
        },
        PUBLIC {
            override fun toLongStyleString(context: Context): String {
                return context.resources.getText(R.string.public_voting_long).toString()
            }

            override fun toString(context: Context): String {
                return context.resources.getText(R.string.public_voting).toString()
            }
        },
        SELECTED {
            override fun toLongStyleString(context: Context): String {
                return context.resources.getText(R.string.selected_judges).toString()
            }

            override fun toString(context: Context): String {
                return context.resources.getText(R.string.selected_judges).toString()
            }
        },
        NONE {
            override fun toLongStyleString(context: Context): String {
                return context.resources.getText(R.string.no_voting_used).toString()
            }

            override fun toString(context: Context): String {
                return context.resources.getText(R.string.no_voting_used).toString()
            }
        };

        abstract fun toString(context: Context): String
        abstract fun toLongStyleString(context: Context): String
    }


    enum class VotingLength {
        TWENTY_FOUR_HOURS {
            override fun toString(context: Context): String {
                return context.resources.getStringArray(R.array.VotingLengths)[0]
            }
        },
        THREE_DAYS {
            override fun toString(context: Context): String {
                return context.resources.getStringArray(R.array.VotingLengths)[1]
            }
        },
        ONE_WEEK {
            override fun toString(context: Context): String {
                return context.resources.getStringArray(R.array.VotingLengths)[2]
            }
        },
        ONE_MONTH {
            override fun toString(context: Context): String {
                return context.resources.getStringArray(R.array.VotingLengths)[3]
            }
        };

        abstract fun toString(context: Context): String
    }



}
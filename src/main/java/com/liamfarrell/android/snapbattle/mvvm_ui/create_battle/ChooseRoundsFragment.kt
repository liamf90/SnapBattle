package com.liamfarrell.android.snapbattle.mvvm_ui.create_battle

import android.os.Bundle
import android.view.*
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.FragmentChooseRoundsBinding


class ChooseRoundsFragment : Fragment() {
    private lateinit var numberPicker: NumberPicker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceBundle: Bundle?): View? {
        val  binding = FragmentChooseRoundsBinding.inflate(inflater, container, false)
        numberPicker = binding.numberOfRounds
        numberPicker.maxValue = 5
        numberPicker.minValue = 1
        setToolbar(binding.includeToolbar.toolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Choose Rounds"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater : MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_create_battle, menu);
    }


    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        val id = item.itemId
        if (id == R.id.action_next) {
            val bundle = bundleOf(("rounds" to numberPicker.value))
            bundle.putAll(arguments)
            findNavController().navigate(R.id.action_chooseRoundsFragment_to_chooseVotingFragment, bundle)
        }
        return false
    }

}
package com.liamfarrell.android.snapbattle.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment


class VotingLengthAdapter(private val con: Context) : ArrayAdapter<ChooseVotingFragment.VotingLength>(con, 0, ChooseVotingFragment.VotingLength.values()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var checkedTextView = convertView as CheckedTextView?

        if (checkedTextView == null) {
            checkedTextView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false) as CheckedTextView
        }
        checkedTextView.text = getItem(position)?.toString(con) ?: ""
        return checkedTextView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var checkedTextView = convertView as CheckedTextView?

        if (checkedTextView == null) {
            checkedTextView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false) as CheckedTextView
        }

        checkedTextView.text = getItem(position)!!.toString(con)

        return checkedTextView
    }
}
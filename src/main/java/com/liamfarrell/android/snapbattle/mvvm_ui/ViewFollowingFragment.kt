package com.liamfarrell.android.snapbattle.mvvm_ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.FollowingListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentViewFollowersBinding
import com.liamfarrell.android.snapbattle.viewmodels.FollowingViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.liamfarrell.android.snapbattle.di.Injectable
import kotlinx.android.synthetic.main.fragment_completed_list.view.*
import kotlinx.android.synthetic.main.fragment_view_followers.*
import javax.inject.Inject


class ViewFollowingFragment : Fragment() , Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var viewModel: FollowingViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentViewFollowersBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this, viewModelFactory).get(FollowingViewModel::class.java)
        val adapter = FollowingListAdapter(::removeFollowing, ::addFollowing)
        binding.recyclerList.adapter = adapter
        binding.recyclerList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.EnterUsernameManuallyButton.setOnClickListener{launchEnterUsernameDialog()}
        binding.viewModel = viewModel

        subscribeUi(binding, adapter)
        return binding.root
    }

    private fun subscribeUi(binding : FragmentViewFollowersBinding, adapter: FollowingListAdapter) {
        viewModel.following.observe(viewLifecycleOwner, Observer { followingList ->
            adapter.submitList(followingList)
            adapter.notifyDataSetChanged()


            if (followingList.isEmpty()) binding.noFollowingTextView.visibility = View.VISIBLE
            else binding.noFollowingTextView.visibility = View.GONE
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun launchEnterUsernameDialog() {
            val v2 = activity!!.layoutInflater.inflate(R.layout.username_dialog, null)
             val usernameEditText = v2.findViewById<View>(R.id.usernameTextView) as EditText
            AlertDialog.Builder(activity)
                    .setView(v2)
                    .setTitle(R.string.enter_username_dialog_title)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.cancel()
                        viewModel.addFollowing(usernameEditText.text.toString())
                    }
                    .create().show()
    }

    private fun addFollowing(cognitoId: String) {
        viewModel.followUser(cognitoId)
    }

    private fun removeFollowing(cognitoId: String) {
        viewModel.removeFollowing(cognitoId)
    }


}
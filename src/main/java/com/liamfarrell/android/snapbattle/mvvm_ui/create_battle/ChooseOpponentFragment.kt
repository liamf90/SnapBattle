package com.liamfarrell.android.snapbattle.mvvm_ui.create_battle

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.ChooseOpponentListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentOpponentListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.model.User
import com.liamfarrell.android.snapbattle.viewmodels.create_battle.ChooseOpponentViewModel
import javax.inject.Inject

class ChooseOpponentFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ChooseOpponentViewModel
    private var showNextButton = false
    private var enableNextButton = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentOpponentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this, viewModelFactory).get(ChooseOpponentViewModel::class.java)
        val adapter = ChooseOpponentListAdapter(::opponentSelected)
        binding.recyclerList.adapter = adapter
        binding.recyclerList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.recyclerList.itemAnimator = null
        binding.viewModel = viewModel
        binding.EnterUsernameManuallyButton.setOnClickListener(getOnEnterUsernameButtonClickListener(viewModel))
        binding.opponentSelectorTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 -> viewModel.recentOpponentsTabSelected()
                    1 -> viewModel.followingTabSelected()
                    2 -> viewModel.facebookFriendsTabSelected()
                }
            }
        })

        binding.searchbox.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {return true}
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterResults(newText ?: "")
                return true
            }
        })

        subscribeUi(adapter)
        setHasOptionsMenu(true)
        setToolbar(binding.toolbar.toolbar)
        viewModel.recentOpponentsTabSelected()
        return binding.root
    }

    private fun opponentSelected(user: User){
        if (user.cognitoId != null) {
            val bundle = bundleOf(("opponentCognitoId" to user.cognitoId), ("opponentUsername" to user.username) )
            bundle.putAll(arguments)
            findNavController().navigate(R.id.action_chooseOpponentFragment_to_chooseRoundsFragment, bundle)
        } else if (user.facebookUserId != null){
            val bundle = bundleOf(("opponentFacebookId" to user.facebookUserId),("opponentName" to user.facebookName) )
            bundle.putAll(arguments)
            findNavController().navigate(R.id.action_chooseOpponentFragment_to_chooseRoundsFragment, bundle)
        }
    }

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Choose Opponent";
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false);
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater : MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_create_battle, menu);
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_next).isEnabled = enableNextButton
        menu.findItem(R.id.action_next).isVisible = showNextButton
    }


    private fun subscribeUi(adapter: ChooseOpponentListAdapter) {
        viewModel.userListFilteredBySearch.observe(viewLifecycleOwner, Observer { userList ->
            userList?.let{adapter.submitList(it)}
        })

        viewModel.userList.observe(viewLifecycleOwner, Observer { userList ->
            userList?.let{adapter.submitList(it)}
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }

    private fun getOnEnterUsernameButtonClickListener(viewModel: ChooseOpponentViewModel) : View.OnClickListener{
        return View.OnClickListener {
                val v2 = activity!!.layoutInflater
                        .inflate(R.layout.username_dialog, null)
                val usernameEditText = v2.findViewById<EditText>(R.id.usernameTextView)

                AlertDialog.Builder(activity)
                        .setView(v2)
                        .setTitle(R.string.enter_username_dialog_title)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.cancel()
                            viewModel.usernameEnteredManually(usernameEditText.text.toString(), ::opponentSelected)
                        }
                        .create().show()
            }
     }

}
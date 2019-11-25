package com.liamfarrell.android.snapbattle.mvvm_ui.startup

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.FragmentChooseNameStartupBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.viewmodels.startup.ChooseNameStartupViewModel
import java.lang.ClassCastException
import javax.inject.Inject


class ChooseNameStartupFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var startupActivity : SetupToolbarInterface
    private lateinit var viewModel: ChooseNameStartupViewModel
    private lateinit var defaultName : String
    private lateinit var binding: FragmentChooseNameStartupBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SetupToolbarInterface){
            startupActivity = context
            startupActivity.setTitle(resources.getStringArray(R.array.startup_activity_titles)[1])
        } else {
            throw ClassCastException() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        defaultName = arguments?.getString("defaultName") ?: ""
        binding = FragmentChooseNameStartupBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.nameEditText.setText(defaultName)
        binding.nameEditText.addTextChangedListener(getNameOnChangedListener())
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChooseNameStartupViewModel::class.java)
        binding.viewModel = viewModel
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }

    @SuppressLint("ApplySharedPref")
    fun updateName() {
        val newName = binding.nameEditText.getText().toString()
        if (defaultName == newName) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
            sharedPref.edit().putString(LoggedInFragment.NAME_SHAREDPREFS, newName).commit()
            findNavController().navigate(R.id.action_chooseNameStartupFragment_to_chooseProfilePictureStartupFragment, arguments)
        } else {
            viewModel.updateName(newName) {findNavController().navigate(R.id.action_chooseNameStartupFragment_to_chooseProfilePictureStartupFragment, arguments)}
        }
    }


    private fun getNameOnChangedListener() = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (s.isNotEmpty()) {
                    startupActivity.enableNextButton()
                } else {
                    startupActivity.disableNextButton()
                }
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        val id = item.itemId
        if (id == R.id.action_next) {
            updateName()
        } else if (id == R.id.action_skip) {
            findNavController().navigate(R.id.action_chooseNameStartupFragment_to_chooseProfilePictureStartupFragment, arguments)
        }
        return false
    }





}
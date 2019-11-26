package com.liamfarrell.android.snapbattle.mvvm_ui.startup

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.databinding.FragmentChooseUsernameStartupBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UpdateUsernameRequest
import com.liamfarrell.android.snapbattle.viewmodels.startup.ChooseUsernameStartupViewModel
import kotlinx.android.synthetic.main.fragment_choose_username_startup.*
import java.lang.ClassCastException
import javax.inject.Inject





class ChooseUsernameStartupFragment : Fragment() , Injectable {

    companion object{
        val usernamameNotValidErorrCode = "USERNAME_NOT_VALID"
        val usernameTooLongErrorCode = "USERNAME_TOO_LONG"
        val usernameUsernameAlreadyExistsErrorCode = "USERNAME_ALREADY_EXISTS"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var startupActivity : SetupToolbarInterface
    private lateinit var viewModel: ChooseUsernameStartupViewModel
    private lateinit var defaultUsername : String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SetupToolbarInterface){
            startupActivity = context
            startupActivity.setTitle(resources.getStringArray(R.array.startup_activity_titles)[3])
        } else {
            throw ClassCastException() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        defaultUsername = arguments?.getString("defaultUsername") ?: ""
        setHasOptionsMenu(true)
        (activity as StartupActivity).disableNextButton()
        val binding = FragmentChooseUsernameStartupBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.usernameEditText.setText(defaultUsername)
        binding.usernameEditText.addTextChangedListener(getUsernameOnChangedListener())
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChooseUsernameStartupViewModel::class.java)
        binding.viewModel = viewModel
        subscribeUi()
        return binding.root
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        val id = item.itemId
        if (id == R.id.action_next) {
            updateUsername()
        } else if (id == R.id.action_skip) {
            nextActivity()
        }
        return false
    }

    private fun subscribeUi() {
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }

    private fun nextActivity() {
        findNavController().navigate(R.id.action_chooseUsernameStartupFragment_to_mainActivity)
    }

    @SuppressLint("ApplySharedPref")
    fun updateUsername() {
        val newUsername = usernameEditText.getText().toString()
        if (defaultUsername == newUsername) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
            sharedPref.edit().putString(LoggedInFragment.USERNAME_SHAREDPREFS, newUsername).commit()
            nextActivity()
        } else {
            val request = UpdateUsernameRequest()
            request.username = newUsername
            viewModel.updateUsername(newUsername, ::nextActivity)
        }
    }


    private fun getUsernameOnChangedListener() = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (s.isNotEmpty()) {
                    (activity as StartupActivity).enableNextButton()
                } else {
                    (activity as StartupActivity).disableNextButton()
                }
                //make username all lowercase
                if (s.toString() != s.toString().toLowerCase()) {
                    usernameEditText.setText(s.toString().toLowerCase())
                    usernameEditText.setSelection(usernameEditText.getText().toString().length)
                }
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }



}
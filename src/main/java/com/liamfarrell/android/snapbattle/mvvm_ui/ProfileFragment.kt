package com.liamfarrell.android.snapbattle.mvvm_ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.ProfilePicRepository
import com.liamfarrell.android.snapbattle.databinding.FragmentProfileBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.util.hideKeyboard
import com.liamfarrell.android.snapbattle.viewmodels.ProfileViewModel
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File
import javax.inject.Inject

class ProfileFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var profilePicRepository : ProfilePicRepository

    private lateinit var viewModel: ProfileViewModel
    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var changeNameButton : Button
    private lateinit var changeUsernameButton : Button
    private lateinit var profileImageView : de.hdodenhof.circleimageview.CircleImageView

    companion object{
        private val PICK_IMAGE = 400
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)

        nameEditText = binding.nameEditText
        usernameEditText = binding.usernameEditText
        changeNameButton = binding.changeNameButton
        changeUsernameButton = binding.changeUsernameButton
        profileImageView = binding.profileImageView

        binding.viewModel = viewModel
        binding.updateProfilePicOnClickListener = View.OnClickListener {
            CropImage.activity().setAspectRatio(200, 200).start(requireContext(), this)
        }
        binding.updateNameOnClickListener = View.OnClickListener{
            changeNameButton.visibility = View.GONE
            viewModel.updateName(binding.usernameEditText.text.toString())
        }

        binding.updateUsernameOnClickListener = View.OnClickListener{
            changeUsernameButton.visibility = View.GONE
            viewModel.updateUsername(binding.usernameEditText.text.toString(), requireContext())
        }
        setProfilePic()
        subscribeUi(binding)
        viewModel.getProfile(::setProfilePic)
        addTextChangedListeners()
        return binding.root
    }



    private fun subscribeUi(binding : FragmentProfileBinding) {
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                val coordinatorLayout = binding.coordinatorLayout
                val snackBar = Snackbar.make(coordinatorLayout, it, Snackbar.LENGTH_SHORT)
                snackBar.show()
            }
        })
    }


    private fun setProfilePic(){
        Picasso.get().load(File(viewModel.getProfilePicPath())).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(profileImageView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE) {
            val chosenPhotoUri = data!!.data
            CropImage.activity(chosenPhotoUri).setAspectRatio(200, 200).start(requireContext(), this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                profileImageView.setImageURI(resultUri)
                resultUri.path?.let{viewModel.uploadProfilePic(it)}
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(activity, R.string.generic_error_toast, Toast.LENGTH_SHORT).show() }
        }
    }


    private fun addTextChangedListeners() {
        nameEditText.setOnEditorActionListener(DoneOnEditorActionListener)
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (viewModel.profile.value?.facebookName == null || viewModel.profile.value?.facebookName != nameEditText.text.toString()) {
                    changeNameButton.visibility = View.VISIBLE
                } else {
                    changeNameButton.visibility = View.GONE
                }
            }
        })

        usernameEditText.setOnEditorActionListener(DoneOnEditorActionListener)
        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (viewModel.profile.value?.username == null || viewModel.profile.value?.username != usernameEditText.text.toString()) {
                    changeUsernameButton.visibility = View.VISIBLE
                } else {
                    changeUsernameButton.visibility = View.GONE
                }

                if (s.toString() != s.toString().toLowerCase()) {
                    usernameEditText.setText(s.toString().toLowerCase())
                    usernameEditText.setSelection(usernameEditText.getText().toString().length)
                }
            }

        })
    }



    object DoneOnEditorActionListener : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v?.context?.hideKeyboard(v)
                return true
            }
            return false
        }
    }



}
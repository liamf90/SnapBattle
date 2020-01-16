package com.liamfarrell.android.snapbattle.mvvm_ui.startup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.ProfilePicRepository
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_choose_profile_picture_startup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ChooseProfilePictureStartupFragment : Fragment(), Injectable, CoroutineScope {

    companion object {
        const val PICK_IMAGE = 400
        const val PROFILE_PIC_UPDATED_BROADCAST = "com.liamfarrell.android.snapbattle.profilepicupdatedbroadcast"
    }

    private lateinit var startupActivity : SetupToolbarInterface
    @Inject
    lateinit var profilePicRepository : ProfilePicRepository
    private var tempSavedProfilePicPath: String? = null
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SetupToolbarInterface){
            startupActivity = context
            startupActivity.setTitle(resources.getStringArray(R.array.startup_activity_titles)[2])
        } else {
            throw ClassCastException() }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_choose_profile_picture_startup, parent, false)
        setHasOptionsMenu(true)
        job = Job()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressContainer.visibility = View.GONE
        profileImageView.setImageResource(R.drawable.default_profile_pic)
        changeProfilePictureButton.setOnClickListener { CropImage.activity().setAspectRatio(200, 200).start(activity!!, this) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE) {
            //image has been selected, now crop it
            val chosenPhotoUri = data!!.data
            CropImage.activity(chosenPhotoUri).setAspectRatio(200, 200).start(context!!, this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            //image has been cropped
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                profileImageView.setImageURI(resultUri)
                (activity as StartupActivity).enableNextButton()
                tempSavedProfilePicPath = resultUri.path
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(activity, R.string.generic_error_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        val id = item.itemId
        if (id == R.id.action_next) {
            progressContainer.visibility = View.VISIBLE
            tempSavedProfilePicPath?.let {
                uploadProfilePic(it)
            } ?: findNavController().navigate(R.id.action_chooseProfilePictureStartupFragment_to_chooseUsernameStartupFragment, arguments)
        } else if (id == R.id.action_skip) {
            findNavController().navigate(R.id.action_chooseProfilePictureStartupFragment_to_chooseUsernameStartupFragment, arguments)
        }
        return false
    }

    private fun uploadProfilePic(profilePicPath: String) {
        launch {
            val response = profilePicRepository.uploadProfilePicRepository(requireContext(),profilePicPath, 0)
            if (response.error != null){
                Toast.makeText(requireContext(), getErrorMessage(requireContext(), response.error), Toast.LENGTH_SHORT).show()
            } else {
                //Send broadcast to update the profile pic
                val intent = Intent(PROFILE_PIC_UPDATED_BROADCAST)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                findNavController().navigate(R.id.action_chooseProfilePictureStartupFragment_to_chooseUsernameStartupFragment, arguments)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


}
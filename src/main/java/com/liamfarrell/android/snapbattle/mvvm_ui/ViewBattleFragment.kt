package com.liamfarrell.android.snapbattle.mvvm_ui

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amazonaws.mobile.auth.core.IdentityManager
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.BattleVideoAdapter
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.databinding.FragmentViewBattleBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Video
import com.liamfarrell.android.snapbattle.mvvm_ui.to_be_converted.VideoRecordActivity
import com.liamfarrell.android.snapbattle.service.MyGcmListenerService
import com.liamfarrell.android.snapbattle.util.downloadFileFromURL
import com.liamfarrell.android.snapbattle.viewmodels.ViewOwnBattleViewModel
import kotlinx.android.synthetic.main.fragment_view_comments.*
import javax.inject.Inject

class ViewBattleFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var usersBattleRepository : UsersBattleRepository

    companion object{
        const val BATTLE_ID_EXTRA = "com.liamfarrell.android.snapbattle.battle_id_extra"
        const val VIDEO_ID_EXTRA = "com.liamfarrell.android.snapbattle.videoidextra"
        const val VIDEO_FILEPATH_EXTRA = "com.liamfarrell.android.snapbattle.videofilepathextra"
        const val USER_BANNED_ERROR = "USER_BANNED_ERROR"
        const val WRITE_EXTERNAL_REQUEST_CODE = 30 }


    private lateinit var viewModel: ViewOwnBattleViewModel
    private lateinit var battle : Battle
    private lateinit var binding : FragmentViewBattleBinding
    private lateinit var adapter : BattleVideoAdapter
    private var battleID = -1
    private val args: ViewBattleFragmentArgs by navArgs()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentViewBattleBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        battleID = args.battleId
        viewModel = ViewModelProvider(this, viewModelFactory).get(ViewOwnBattleViewModel::class.java)
        binding.viewModel = viewModel
        binding.included.saveToDeviceButton.setOnClickListener {onSaveToDeviceButtonClicked()}
        binding.included.playWholeBattleButton.setOnClickListener {onPlayButtonClicked()}
        binding.challengerNameTextView.setOnClickListener {goToChallengerUsersBattles()}
        binding.challengedNameTextView.setOnClickListener {goToChallengedUsersBattles()}
        binding.included.viewCommentsButton.setOnClickListener { showCommentsFragment() }
        registerReceiver()
        subscribeUi()
        viewModel.getBattle(battleID)
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.battle.observe(viewLifecycleOwner, Observer {
            it?.let{
            battle = it
            adapter = BattleVideoAdapter(battle, ::getRecordButtonOnClick, ::onVideoSubmitted, usersBattleRepository )
            binding.recyclerView.adapter = adapter
            binding.battleStatus = battle.getBattleStatus(requireContext())
            adapter.submitList(it.videos.toList())}
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, Observer {snackBarMessage ->
            snackBarMessage?.let{Snackbar.make(parentCoordinatorLayout, snackBarMessage, Snackbar.LENGTH_LONG).show()}
        })
    }

    override fun onDestroy() {
        destroyRegister()
        super.onDestroy()
    }




    /**
     * Called after returned from [VideoRecordActivity].
     * Update the views if a new video has been recorded
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_CANCELED) {
            //this is called when an error has occurred. video file may be deleted. reload list to disable play button
            adapter.notifyDataSetChanged()
        }
        if (requestCode == 100) {
            if (data == null) {
                return }

            if (data.getStringExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK) != null) {
                if (data.getStringExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK) == Battle.ORIENTATION_LOCK_PORTRAIT) {
                    battle.setOrientationLock(Battle.ORIENTATION_LOCK_PORTRAIT)
                }
                if (data.getStringExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK) == Battle.ORIENTATION_LOCK_LANDSCAPE) {
                    battle.setOrientationLock(Battle.ORIENTATION_LOCK_LANDSCAPE)
                }
            }
            viewModel.getBattle(battleID)
        }
    }

    /**
     * When a video is submitted by the opponent, a GCM is sent to the user and a broadcast is sent out.
     * Receivers are used by this fragment to receive the broadcast and update the fragment
     */
    private fun registerReceiver() {
        //register receivers to update the list when a video is submitted (if fragment are still visible)
        val filter = IntentFilter()
        filter.addAction(MyGcmListenerService.ACTION_VIDEO_SUBMITTED)
        filter.addAction(MyGcmListenerService.ACTION_FULL_VIDEO_FINISHED)
        activity?.registerReceiver(mOnShowNotification, filter, MyGcmListenerService.PERM_PRIVATE, null)
    }



    private fun destroyRegister() {
        activity?.unregisterReceiver(mOnShowNotification)
    }

    private val mOnShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra(MyGcmListenerService.TYPE_INTENT_EXTRA) == MyGcmListenerService.TYPE_VIDEO_SUBMITTED && intent.getIntExtra(MyGcmListenerService.BATTLE_ID_INTENT_EXTRA, -1) == battle.getBattleId()) {
                // TODO If we receive this, we're visible, so cancel the notification
                //setResultCode(Activity.RESULT_CANCELED);
                viewModel.getBattle(battleID)
            } else if (intent.getStringExtra(MyGcmListenerService.TYPE_INTENT_EXTRA) == MyGcmListenerService.UPLOAD_FULL_VIDEO && intent.getIntExtra(MyGcmListenerService.BATTLE_ID_INTENT_EXTRA, -1) == battle.getBattleId()) {
                viewModel.getBattle(battleID)
            }
        }
    }


    private fun onSaveToDeviceButtonClicked(){
        if (isStoragePermissionGranted()) {
            saveFileToDevice()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return context?.let {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(it, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    return true
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_REQUEST_CODE)
                    return false
                }
            } else { //permission is automatically granted on sdk<23 upon installation
                return true
            }
        } ?: false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //resume tasks needing this permission
            saveFileToDevice()
        }
    }


    private fun saveFileToDevice() {
        val filepath = battle.getServerFinalVideoUrl(IdentityManager.getDefaultIdentityManager().cachedUserID)
        val callback = Battle.SignedUrlCallback { signedUrl ->
            context?.let{downloadFileFromURL(it, signedUrl, battle.finalVideoFilename, null, battle.battleName)}
        }
        Battle.getSignedUrlFromServer(filepath, context, callback)
    }

    private fun onVideoSubmitted() {
        viewModel.getBattle(battleID)
    }

    private fun goToChallengerUsersBattles(){
        val direction =  ViewBattleFragmentDirections.actionViewBattleFragmentToNavigationUsersBattles(battle.challengerCognitoID)
        findNavController().navigate(direction)
    }

    private fun goToChallengedUsersBattles(){
        val direction = ViewBattleFragmentDirections.actionViewBattleFragmentToNavigationUsersBattles(battle.challengedCognitoID)
        findNavController().navigate(direction)
    }

    private fun showCommentsFragment(){
        val direction = ViewBattleFragmentDirections.actionViewBattleFragmentToViewCommentsFragment(battle.battleId)
        findNavController().navigate(direction)
    }


    private fun onPlayButtonClicked() {
        activity?.let {
                //Stream the file
                val filepath = battle.getServerFinalVideoUrl(IdentityManager.getDefaultIdentityManager().cachedUserID)
                val callback = Battle.SignedUrlCallback { signedUrl ->
                    progressContainer.setVisibility(View.GONE)
                    val direction =  ViewBattleFragmentDirections.actionViewBattleFragmentToVideoViewFragment(signedUrl)
                    findNavController().navigate(direction)
                }
                Battle.getSignedUrlFromServer(filepath, it, callback)
                progressContainer.setVisibility(View.VISIBLE) }
    }

    private fun getRecordButtonOnClick(video: Video){
            Dexter.withActivity(activity)
                    .withPermissions(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.RECORD_AUDIO
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            // check if all permissions are granted
                            if (report.areAllPermissionsGranted()) {
                                val i = Intent(activity, VideoRecordActivity::class.java)
                                i.putExtra(VIDEO_ID_EXTRA, video.getVideoID())
                                if (video.getVideoNumber() == 1) {
                                    i.putExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK, Battle.ORIENTATION_LOCK_UNDEFINED)
                                } else {
                                    i.putExtra(VideoRecordActivity.EXTRA_ORIENTATION_LOCK, battle.getOrientationLock())
                                }
                                startActivityForResult(i, 100)
                            }
                            // check for permanent denial of any permission
                            if (report.isAnyPermissionPermanentlyDenied) {
                                // permission is denied permenantly, navigate user to app settings
                                // check for permanent denial of permission
                                showSettingsDialog() }
                        }
                        override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                            token.continuePermissionRequest()
                        }
                    })
                    .withErrorListener { context?.let{Toast.makeText(it.applicationContext, R.string.generic_error_toast, Toast.LENGTH_SHORT).show()} }
                    .onSameThread()
                    .check()
        }


    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and messageTextView depending on your app
     */
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(R.string.app_permission_dialog_title)
        builder.setMessage(R.string.app_need_permissions_not_given)
        builder.setPositiveButton(R.string.app_permission_goto_settings) { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(R.string.app_permission_cancel) { dialog, _ -> dialog.cancel() }
        builder.show()

    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity!!.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

}
package com.liamfarrell.android.snapbattle.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.auth.core.IdentityManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.databinding.ListItemCommentBinding
import com.liamfarrell.android.snapbattle.databinding.ListItemVideoUserBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Comment
import com.liamfarrell.android.snapbattle.model.Video
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment
import com.liamfarrell.android.snapbattle.ui.VideoRecordActivity
import com.liamfarrell.android.snapbattle.ui.VideoViewActivity
import com.liamfarrell.android.snapbattle.ui.VideoViewFragment
import com.liamfarrell.android.snapbattle.viewmodels.BattleVideoItemViewModel
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
* Adapter for the [RecyclerView] in [ViewBattleFragment].
*/
class BattleVideoAdapter (val battle: Battle, val recordButtonOnClickCallback: (video: Video)-> Unit,  private val videoUploadedCallback : () -> Unit, private val usersBattleRepository : UsersBattleRepository) :
        ListAdapter<Video, BattleVideoAdapter.ViewHolder>(VideoDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_video_user, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = getItem(position)
        val viewModel =  BattleVideoItemViewModel(holder.itemView.context.applicationContext, battle, video, usersBattleRepository)
        viewModel.errorMessage.observeForever(Observer { Toast.makeText(holder.itemView.context, it, Toast.LENGTH_SHORT).show() })

        holder.apply {
            bind(video,battle, viewModel,
                    createRecordButtonOnClickListener(video),
                    createPlayButtonOnClickListener(itemView.context, video),
                    createSubmitButtonOnClickListener(viewModel))
        }
    }


    class ViewHolder(
             val binding: ListItemVideoUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Video, battle: Battle,
                 videoViewModel: BattleVideoItemViewModel,
                 recordButtonOnClickListener: View.OnClickListener,
                 playButtonOnClickListener : View.OnClickListener,
                 submitButtonOnClickListener: View.OnClickListener) {
            with(binding) {
                video = item
                viewModel = videoViewModel
                recordButtonOnClick = recordButtonOnClickListener
                playButtonOnClick = playButtonOnClickListener
                submitButtonOnClick = submitButtonOnClickListener
                executePendingBindings()
            }
        }
    }




    private fun createPlayButtonOnClickListener(context: Context, video: Video) : View.OnClickListener{
        return View.OnClickListener {
            val filepath = context.getFilesDir().getAbsolutePath() + "/" + video.getVideoFilename()
            val file = File(filepath)

            if (!file.exists()) {
                playWithCloudFrontSignedUrl(context, video.getVideoFilename())
            } else {
                //Go to view video
                // val intent = Intent(getCallbacks(), VideoViewActivity::class.java)
                //intent.putExtra(VIDEO_FILEPATH_EXTRA, filepath)
                //startActivity(intent)
            }
        }
    }

    private fun createSubmitButtonOnClickListener(viewModel: BattleVideoItemViewModel) : View.OnClickListener{
        return View.OnClickListener {
            GlobalScope.launch {
                viewModel.uploadVideo()
                videoUploadedCallback()
            }
        }
    }

    private fun createRecordButtonOnClickListener(video: Video) : View.OnClickListener{
        return View.OnClickListener {
            recordButtonOnClickCallback(video)
        }
    }


    private fun playWithCloudFrontSignedUrl(context: Context, s3Path: String) {
        val url = "https://djbj27vmux1mw.cloudfront.net/" + IdentityManager.getDefaultIdentityManager().getCachedUserID() + "/" + s3Path
        Battle.getSignedUrlFromServer(url, context) { signedUrl ->
            val intent = Intent(context, VideoViewActivity::class.java)
            intent.putExtra(VideoViewFragment.VIDEO_FILEPATH_EXTRA, signedUrl)
            //startActivity(intent)
        }
    }




}


private class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {

    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem.videoID == newItem.videoID
    }

    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }
}
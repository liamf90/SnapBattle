package com.liamfarrell.android.snapbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.UsersBattleRepository
import com.liamfarrell.android.snapbattle.databinding.ListItemVideoUserBinding
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Video
import com.liamfarrell.android.snapbattle.mvvm_ui.ViewBattleFragmentDirections
import com.liamfarrell.android.snapbattle.viewmodels.BattleVideoItemViewModel
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
                    createPlayButtonOnClickListener(video),
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




    private fun createPlayButtonOnClickListener(video: Video) : View.OnClickListener{
        return View.OnClickListener {
            val filepath = it.context.getFilesDir().getAbsolutePath() + "/" + video.getVideoFilename()
            val file = File(filepath)

            if (!file.exists()) {
                playWithCloudFrontSignedUrl(it, video.videoFilename)
            } else {
                //Go to view video
                val direction =   ViewBattleFragmentDirections.actionViewBattleFragmentToVideoViewFragment(filepath)
                it.findNavController().navigate(direction)
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


    private fun playWithCloudFrontSignedUrl(view: View, s3Path: String) {
        val url = "https://djbj27vmux1mw.cloudfront.net/" + IdentityManager.getDefaultIdentityManager().getCachedUserID() + "/" + s3Path
        Battle.getSignedUrlFromServer(url, view.context) { signedUrl ->
            val direction =  ViewBattleFragmentDirections.actionViewBattleFragmentToVideoViewFragment(signedUrl)
            view.findNavController().navigate(direction)
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
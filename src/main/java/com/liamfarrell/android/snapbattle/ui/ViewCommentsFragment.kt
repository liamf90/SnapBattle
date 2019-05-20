package com.liamfarrell.android.snapbattle.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.databinding.FragmentViewCommentsBinding
import com.liamfarrell.android.snapbattle.di.AWSLambdaModule
import com.liamfarrell.android.snapbattle.di.CommentViewModelFactoryModule
import com.liamfarrell.android.snapbattle.di.DaggerAppComponent
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModel
import com.liamfarrell.android.snapbattle.adapters.CommentsListAdapter


class ViewCommentsFragment : Fragment(){

    companion object{
       val EXTRA_BATTLEID = "com.liamfarrell.android.snapbattle.viewcommentsfragment.battleIDextra"}

    private lateinit var viewModel : CommentViewModel
    private lateinit var progressContainer : FrameLayout

    private var battleID = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentViewCommentsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        battleID = getActivity()?.getIntent()?.getIntExtra(FullBattleVideoPlayerActivity.EXTRA_BATTLEID, 0)!!;
        if (battleID == 0)
        {
            battleID = getActivity()?.getIntent()?.getIntExtra(EXTRA_BATTLEID, 0)!!;
        }

        val appComponent  = DaggerAppComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .commentViewModelFactoryModule(CommentViewModelFactoryModule(battleID))
                .build()



        viewModel = ViewModelProviders.of(this, appComponent.getCommentViewModelFactory()).get(CommentViewModel::class.java)
        val adapter = CommentsListAdapter()
        binding.commentsList.adapter = adapter
        binding.viewModel = viewModel

        progressContainer = binding.progressContainer
        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: CommentsListAdapter) {
        viewModel.comments.observe(viewLifecycleOwner, Observer {commentsList ->
            adapter.submitList(commentsList)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })


    }


}




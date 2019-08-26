package com.liamfarrell.android.snapbattle.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.adapters.ReportedCommentCallback
import com.liamfarrell.android.snapbattle.adapters.ReportedCommentListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentReportingsBinding
import com.liamfarrell.android.snapbattle.di.AWSLambdaModule
import com.liamfarrell.android.snapbattle.di.DaggerCommentsReportedComponent
import com.liamfarrell.android.snapbattle.di.DaggerCurrentBattlesComponent
import com.liamfarrell.android.snapbattle.viewmodel.CommentsReportedViewModel

class CommentsReportedFragment : Fragment(), ReportedCommentCallback {

    private lateinit var viewModel: CommentsReportedViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentReportingsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val appComponent = DaggerCommentsReportedComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .build()



        viewModel = ViewModelProviders.of(this, appComponent.getCommentsReportedViewModelFactory()).get(CommentsReportedViewModel::class.java)
        val adapter = ReportedCommentListAdapter(this)
        binding.recyclerList.adapter = adapter
        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: ReportedCommentListAdapter) {
        viewModel.reportedComments.observe(viewLifecycleOwner, Observer { reportedCommentsList ->
            adapter.submitList(reportedCommentsList)
        })
    }

    override fun onIgnoreComment(commentId: Int) {
        viewModel.ignoreComment(commentId)
    }

    override fun onDeleteComment(commentId: Int) {
        viewModel.deleteComment(commentId)
    }

    override fun onBanUser(commentId: Int, cognitoIdUserBan: String, banLengthDays: Int) {
        viewModel.banUser(cognitoIdUserBan, commentId, banLengthDays)
    }


}
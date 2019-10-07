package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.text.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.databinding.FragmentViewCommentsBinding
import com.liamfarrell.android.snapbattle.viewmodels.CommentViewModel
import com.liamfarrell.android.snapbattle.adapters.CommentsListAdapter
import android.widget.TextView
import android.text.style.ForegroundColorSpan
import com.liamfarrell.android.snapbattle.util.hideKeyboard
import kotlinx.android.synthetic.main.fragment_view_comments.*
import com.facebook.login.LoginManager
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.facebook.CallbackManager
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.ui.FullBattleVideoPlayerActivity
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject


class ViewCommentsFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    companion object{
       val EXTRA_BATTLEID = "com.liamfarrell.android.snapbattle.viewcommentsfragment.BATTLEIDEXTRA"}

    private lateinit var viewModel : CommentViewModel
    private var isEditTextChangedByOnTextChanged = false
    private var usernameTagsList = mutableListOf<String>()
     val mCallbackManager : CallbackManager by lazy {CallbackManager.Factory.create()}



    private  var battleId = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentViewCommentsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        battleId = arguments?.getInt("battleId") ?: throw IllegalStateException("battle id is not set")

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CommentViewModel::class.java)
        val adapter = CommentsListAdapter(viewModel, ::listItemViewHolderOnClick)
        binding.commentsList.adapter = adapter
        binding.viewModel = viewModel
        binding.commentEditText.addTextChangedListener(onAddCommentEditTextChanged)
        binding.addACommentButton.setOnClickListener(addCommentButtonClicked())
        subscribeUi(adapter)
        viewModel.getComments(battleId)
        return binding.root
    }

    private fun subscribeUi(adapter: CommentsListAdapter) {
        viewModel.comments.observe(viewLifecycleOwner, Observer {commentsList ->
            adapter.submitList(commentsList)
        })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, Observer {snackBarMessage ->
            Snackbar.make(parentCoordinatorLayout, snackBarMessage, Snackbar.LENGTH_LONG).show()
        })
    }

    private fun addCommentButtonClicked() : View.OnClickListener{
        return View.OnClickListener {
            viewModel.addComment(battleId, commentEditText.text.toString(), usernameTagsList, ::requestUserFriendsPermission)
            commentEditText.setText("")
            hideKeyboard()
        }
    }


    private fun listItemViewHolderOnClick(username: String) {
            if (!usernameTagsList.contains(username)) {
                usernameTagsList.add(username)

                val oldStringBuilder = SpannableStringBuilder(commentEditText.text)
                val tag = "@$username"

                if (!(commentEditText.text.isNullOrBlank() || commentEditText.text.toString().get(commentEditText.text.toString().length - 1) == ' ')) {
                    oldStringBuilder.append(" ")
                }

                val start = oldStringBuilder.length
                oldStringBuilder.append(tag)
                oldStringBuilder.setSpan(ForegroundColorSpan(-0x33ab00), start, start + tag.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                isEditTextChangedByOnTextChanged = true
                commentEditText.setText(oldStringBuilder, TextView.BufferType.SPANNABLE)
                commentEditText.setSelection(commentEditText.text.toString().length)
                isEditTextChangedByOnTextChanged = false
            }
    }



     private val onAddCommentEditTextChanged : TextWatcher = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (!isEditTextChangedByOnTextChanged) {
                //Check old tags have not been interrupted

                val strBuilderAfter = SpannableStringBuilder(s)
                val spansAfter = strBuilderAfter.getSpans(0, s.toString().length - 1, ForegroundColorSpan::class.java)
                val currentSelection = commentEditText.getSelectionStart()

                //check the username tags all still match
                var allUsernamesMatched = true
                for (username in usernameTagsList) {
                    var usernameMatched = false
                    for (aSpansAfter in spansAfter) {
                        //check new tag still matches old tag
                        val start2 = strBuilderAfter.getSpanStart(aSpansAfter)
                        val end2 = strBuilderAfter.getSpanEnd(aSpansAfter)
                        val tag2 = strBuilderAfter.toString().substring(start2, end2)
                        val usernameSpan = tag2.substring(1, tag2.length)
                        if (username == usernameSpan) {
                            usernameMatched = true
                            break
                        }
                    }
                    if (!usernameMatched) {
                        //remove tag from the username tag list and remove span
                        allUsernamesMatched = false
                        usernameTagsList.remove(username)
                    }
                }

                if (!allUsernamesMatched) {
                    //update spans
                    val newStringBuilder = SpannableStringBuilder(s)
                    newStringBuilder.clearSpans()
                    for (aSpansAfter in spansAfter) {
                        val start3 = strBuilderAfter.getSpanStart(aSpansAfter)
                        val end3 = strBuilderAfter.getSpanEnd(aSpansAfter)
                        val tag3 = strBuilderAfter.toString().substring(start3, end3)
                        val username3 = tag3.substring(1, tag3.length)
                        if (usernameTagsList.contains(username3)) {
                            newStringBuilder.setSpan(ForegroundColorSpan(-0x33ab00), start3, end3, strBuilderAfter.getSpanFlags(aSpansAfter))
                        }
                    }
                    isEditTextChangedByOnTextChanged = true
                    commentEditText.setText(newStringBuilder, TextView.BufferType.SPANNABLE)
                    commentEditText.setSelection(currentSelection)
                    isEditTextChangedByOnTextChanged = false
                }
            }
        }
    }



    /**
     * Methods for verifying that user has over 100 facebook friends, which is required to add a comment
     *
     */



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }


    private fun requestUserFriendsPermission() {
        LoginManager.getInstance().registerCallback(mCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        viewModel.verifyUser(battleId, commentEditText.text.toString(), usernameTagsList)}
                    override fun onCancel() {}
                    override fun onError(e: FacebookException) {
                        Toast.makeText(activity, R.string.server_error_toast, Toast.LENGTH_SHORT).show() }
                })
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("user_friends"))
    }







}




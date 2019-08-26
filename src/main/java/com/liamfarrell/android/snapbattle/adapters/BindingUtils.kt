package com.liamfarrell.android.snapbattle.adapters

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.databinding.BindingAdapter

import com.liamfarrell.android.snapbattle.model.Comment
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import android.view.ViewGroup
import com.liamfarrell.android.snapbattle.R
import com.squareup.picasso.Callback
import timber.log.Timber
import java.lang.Exception

@BindingAdapter("profileImage")
fun loadImage(view: CircleImageView, imageUrl: String?) {
    Picasso.get().load(imageUrl).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(view, object: Callback {
        override fun onSuccess() {
           Timber.i("On Success")
        }

        override fun onError(e: Exception?) {
            Timber.e(e)
        }

    })

}

@BindingAdapter("commentText")
fun bindCommentText(textView: TextView, comment: Comment) {
    val text = SpannableStringBuilder()
            .bold { append( comment.username) }
            .append("  ")
    if (comment.isDeleted){
        text.italic { append( textView.context.resources.getString(R.string.comment_deleted)) }
    } else{
        text.append(comment.comment)
    }
    textView.text = text
}


@BindingAdapter("showProgressAddComment")
fun bindProgressBar(progressLayout: ProgressBar, showSpinner: Boolean) {
    if (showSpinner){
        progressLayout.visibility = View.VISIBLE
    } else {
        progressLayout.visibility = View.GONE
    }
}

@BindingAdapter("layout_marginLeft")
fun setLeftMargin(view: View, leftMargin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(Math.round(leftMargin), layoutParams.topMargin,
            layoutParams.rightMargin, layoutParams.bottomMargin)
    view.layoutParams = layoutParams
}

@BindingAdapter("layout_marginRight")
fun setRightMargin(view: View, rightMargin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
            Math.round(rightMargin), layoutParams.bottomMargin)
    view.layoutParams = layoutParams
}
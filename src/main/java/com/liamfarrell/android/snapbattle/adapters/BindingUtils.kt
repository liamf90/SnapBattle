package com.liamfarrell.android.snapbattle.adapters

import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.databinding.BindingAdapter
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.model.Comment
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber

@BindingAdapter("profileImage")
fun loadImage(view: CircleImageView, imageUrl: String?) {
    //load the image from the cache if it exists, if not, load from server
    Picasso.get().load(imageUrl).placeholder(R.drawable.default_profile_pic100x100).networkPolicy(NetworkPolicy.OFFLINE).error(R.drawable.default_profile_pic100x100).into(view, object : Callback {
        override fun onSuccess() {
            Timber.i("success")
        }

        override fun onError(e: Exception?) {
            Timber.i("url: " + imageUrl + ". error: " + e.toString())
            Picasso.get().load(imageUrl).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(view, object : Callback {
                override fun onSuccess() {
                    Timber.i("success")
                }

                override fun onError(e: Exception?) {
                    Timber.i("url: " + imageUrl + ". error: " + e.toString())
                }


            })
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
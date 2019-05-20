package com.liamfarrell.android.snapbattle.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.databinding.BindingAdapter
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.model.Comment
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


@BindingAdapter("profileImage")
fun loadImage(view: CircleImageView, imageUrl: String) {
    Picasso.get().load(imageUrl).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(view)
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

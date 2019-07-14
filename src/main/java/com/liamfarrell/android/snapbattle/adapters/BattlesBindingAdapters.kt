package com.liamfarrell.android.snapbattle.adapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.liamfarrell.android.snapbattle.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

@BindingAdapter("battleThumbnail")
fun loadImage(view: ImageView, imageUrl: String?) {
    Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder1440x750).error(R.drawable.placeholder1440x750).into(view)
}

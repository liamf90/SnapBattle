package com.liamfarrell.android.snapbattle.adapters

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.liamfarrell.android.snapbattle.R
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber
import java.lang.Exception

@BindingAdapter("battleThumbnail")
fun loadImage(view: ImageView, imageUrl: String?) {

    Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder1440x750).networkPolicy(NetworkPolicy.OFFLINE).error(R.drawable.placeholder1440x750).into(view, object :Callback{
        override fun onSuccess() {
            Timber.i("success")
        }

        override fun onError(e: Exception?) {
           Timber.i("url: " + imageUrl + ". error: " + e.toString())
            Picasso.get().load(imageUrl).placeholder(R.drawable.placeholder1440x750).error(R.drawable.placeholder1440x750).into(view, object :Callback{
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




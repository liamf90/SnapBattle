package com.liamfarrell.android.snapbattle.adapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.liamfarrell.android.snapbattle.R
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import timber.log.Timber

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




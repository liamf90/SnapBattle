package com.liamfarrell.android.snapbattle.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.liamfarrell.android.snapbattle.R
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.suspendCoroutine



// https://stackoverflow.com/questions/9769554/how-to-convert-number-into-k-thousands-m-million-and-b-billion-suffix-in-jsp
// Converts the number to K, M suffix
// Ex: 5500 will be displayed as 5.5k
fun convertToSuffix(count: Long): String {
    if (count < 1000) return "" + count
    val exp = (Math.log(count.toDouble()) / Math.log(1000.0)).toInt()
    return String.format("%.1f%c",
            count / Math.pow(1000.0, exp.toDouble()),
            "kmgtpe"[exp - 1])
}


fun mysqlDateStringToDate(dateString : String) : Date {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.parse(dateString)
}

fun getTimeUntil(context: Context, dateAfterNow: Date): String {
    val cal = Calendar.getInstance()
    cal.timeZone = TimeZone.getTimeZone("UTC")
    val timeNow = cal.time

    return getTimeBetween(context, timeNow, dateAfterNow, false)
}

 fun getTimeBetween(context: Context, before: Date?, after: Date?, shortHandVersion: Boolean): String {
    if (after == null || before == null) {
        return ""
    }


    var timeDifference = after.time - before.time
    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24
    val yearsInMilli = daysInMilli * 365

    val totalMinutes = timeDifference / minutesInMilli
    val totalHours = timeDifference / hoursInMilli
    val totalDays = timeDifference / daysInMilli
    val totalYears = timeDifference / yearsInMilli
    val elapsedYears = timeDifference / yearsInMilli

    val elapsedDays = timeDifference / daysInMilli
    timeDifference = timeDifference % daysInMilli

    val elapsedHours = timeDifference / hoursInMilli
    timeDifference = timeDifference % hoursInMilli

    val elapsedMinutes = timeDifference / minutesInMilli
    var timeSinceString = ""
    val res = context.resources

    if (totalMinutes == 1L) {
        if (shortHandVersion) {
            timeSinceString = elapsedMinutes.toString() + " " + res.getText(R.string.minute_shorthand).toString()
        } else {
            timeSinceString = elapsedMinutes.toString() + " " + res.getText(R.string.minute).toString()
        }
    } else if (totalMinutes < 60) {
        if (shortHandVersion) {
            timeSinceString = elapsedMinutes.toString() + " " + res.getText(R.string.minute_shorthand).toString()
        } else {
            timeSinceString = elapsedMinutes.toString() + " " + res.getText(R.string.minutes).toString()
        }
    } else if (totalHours == 1L) {
        if (shortHandVersion) {
            timeSinceString = elapsedHours.toString() + " " + res.getText(R.string.hour_shorthand).toString()
        } else {
            timeSinceString = elapsedHours.toString() + " " + res.getText(R.string.hour).toString()
        }
    } else if (totalHours < 24) {
        if (shortHandVersion) {
            timeSinceString = elapsedHours.toString() + " " + res.getText(R.string.hour_shorthand).toString()
        } else {
            timeSinceString = elapsedHours.toString() + " " + res.getText(R.string.hours).toString()
        }
    } else if (totalDays == 1L) {
        if (shortHandVersion) {
            timeSinceString = elapsedDays.toString() + " " + res.getText(R.string.days_shorthand).toString()
        } else {
            timeSinceString = elapsedDays.toString() + " " + res.getText(R.string.day).toString()
        }
    } else if (totalDays < 365) {
        if (shortHandVersion) {
            timeSinceString = elapsedDays.toString() + " " + res.getText(R.string.days_shorthand).toString()
        } else {
            timeSinceString = elapsedDays.toString() + " " + res.getText(R.string.days).toString()
        }
    } else if (totalYears == 1L) {
        if (shortHandVersion) {
            timeSinceString = elapsedYears.toString() + " " + res.getText(R.string.years_shorthand).toString()
        } else {
            timeSinceString = elapsedYears.toString() + " " + res.getText(R.string.year).toString()
        }
    } else if (totalYears > 1) {
        if (shortHandVersion) {
            timeSinceString = elapsedYears.toString() + " " + res.getText(R.string.years_shorthand).toString()
        } else {
            timeSinceString = elapsedYears.toString() + " " + res.getText(R.string.years).toString()
        }
    }
    return timeSinceString
}


 suspend fun isSignedUrlInPicassoCache(signedUrl: String) : Boolean{
     return withContext(Dispatchers.Main) {
          suspendCoroutine<Boolean> {
             Timber.i("url: %s", signedUrl)
             Picasso.get().load(signedUrl).networkPolicy(NetworkPolicy.OFFLINE).into(object : Target {
                 override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                 override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                     Timber.i("bitmap not in picasso cache. url= %s", signedUrl)
                     it.resumeWith(Result.success(false))
                 }

                 override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                     Timber.i("bitmap in picasso cache. url = %s", signedUrl)
                     it.resumeWith(Result.success(true))
                 }
             })
         }
     }
}





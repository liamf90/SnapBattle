package com.liamfarrell.android.snapbattle.util

import android.content.Context
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.SnapBattleApp
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.suspendCoroutine


fun mysqlDateStringToDate(dateString : String) : Date {
    var timeBanEnds = Date()
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    timeBanEnds = sdf.parse(dateString)
    return timeBanEnds
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
    return suspendCoroutine<Boolean> {
        Picasso.get().load(signedUrl).networkPolicy(NetworkPolicy.OFFLINE).fetch()
        object : Callback {
            override fun onSuccess() {
                it.resumeWith(Result.success(true))
            }

            override fun onError(e: Exception?) {
                it.resumeWith(Result.success(false))
            }
        }
    }
}





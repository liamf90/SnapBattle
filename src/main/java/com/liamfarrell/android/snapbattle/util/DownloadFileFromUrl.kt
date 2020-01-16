package com.liamfarrell.android.snapbattle.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment


//this function is used to download the battle video before saving it to device
fun downloadFileFromURL(context: Context, inputUrl : String, saveFilename: String, description: String?, title: String ){
        val request = DownloadManager.Request(Uri.parse(inputUrl))
        description?.let{request.setDescription(it)}
         request.setTitle(title)

        // in order for this if to run, you must use the android 3.2 to compile your app
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, saveFilename)

        // get download service and enqueue file
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        manager?.enqueue(request)
}




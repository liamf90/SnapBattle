package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfilePicViewModel @Inject constructor(val otherUsersProfilePicUrlRepository: OtherUsersProfilePicUrlRepository, cognitoID: String, profilePicCount: Int, signedUrlNew: String) {
    private val _signedUrl = MutableLiveData<String>()
    val signedUrl : LiveData<String> = _signedUrl


    init {
            _signedUrl.value = otherUsersProfilePicUrlRepository.getOrUpdateProfilePicSignedUrlRx(cognitoID, profilePicCount, signedUrlNew)
    }






}
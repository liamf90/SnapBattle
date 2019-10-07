package com.liamfarrell.android.snapbattle.mvvm_ui.startup

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.amazonaws.mobile.auth.core.IdentityManager
import com.facebook.AccessToken
import com.liamfarrell.android.snapbattle.MainActivity
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.data.*
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.service.RegistrationIntentService
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment
import com.liamfarrell.android.snapbattle.ui.startup.StartupActivity
import com.liamfarrell.android.snapbattle.util.getErrorMessage
import dagger.android.DispatchingAndroidInjector
import kotlinx.coroutines.*
import java.lang.ClassCastException
import javax.inject.Inject

class LoggedInFragment  : Fragment() , Injectable {
    val USER_ALREADY_EXISTS_RESULT = "USER_ALREADY_EXISTS"
    val USER_ADDED_RESULT = "NEW_USER_ADDED"
    val USERNAME_SHAREDPREFS = "username"
    val NAME_SHAREDPREFS = "facebook_name"
    val COGNITO_ID_SHAREDPREFS = "cognito_id"

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var loginRepository : LoginRepository
    @Inject
    lateinit var followingUserCacheManager : FollowingUserCacheManager
    @Inject
    lateinit var notificationsManager : NotificationsManager
    @Inject
    lateinit var followingBattlesFeedCacheManager: FollowingBattlesFeedCacheManager
    @Inject
    lateinit var allBattlesCacheManager: AllBattlesCacheManager

    private lateinit var startupActivity : SetupToolbarInterface
    private lateinit var job : Job


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        super.onCreate(savedInstanceState)
        val v = inflater.inflate(R.layout.fragment_logged_in, container, false)
        job = GlobalScope.launch(Dispatchers.Main){
            checkIfUserIsRegistered()
        }
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SetupToolbarInterface){
            startupActivity = context
            startupActivity.hideToolbar()
        } else {
            throw ClassCastException() }
    }


    @SuppressLint("ApplySharedPref")
    suspend fun checkIfUserIsRegistered(){
        val facebookId = AccessToken.getCurrentAccessToken().userId
        val facebookNameAsyc = GlobalScope.async(Dispatchers.IO) { loginRepository.getFacebookName() }
        val facebookNameResponse = facebookNameAsyc.await()

        if (facebookNameResponse.error != null){
            Toast.makeText(requireContext(), getErrorMessage(requireContext(), facebookNameResponse.error), Toast.LENGTH_SHORT).show()
            IdentityManager.getDefaultIdentityManager().signOut()
            return
        }
        val responseDeferred = GlobalScope.async(Dispatchers.IO) {loginRepository.createUser(facebookId, facebookNameResponse.result)}
        val response = responseDeferred.await()
        if (response.error != null){
            Toast.makeText(requireContext(), getErrorMessage(requireContext(), response.error), Toast.LENGTH_SHORT).show()
            IdentityManager.getDefaultIdentityManager().signOut()
            return }

        //reset all the room databases for the new logged in user
        resetDatabases()

        //register the user for google cloud messaging
        Intent(requireContext(), RegistrationIntentService::class.java).also { intent ->
            requireContext().startService(intent)
        }

        if (response.result.userExists == USER_ALREADY_EXISTS_RESULT) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext())
            sharedPref.edit().putString(FacebookLoginFragment.NAME_SHAREDPREFS, response.result.name).commit()
            sharedPref.edit().putString(FacebookLoginFragment.USERNAME_SHAREDPREFS, response.result.username).commit()
            findNavController().navigate(R.id.action_loggedInFragment_to_mainActivity)
            activity?.finish()

        } else if (response.result.userExists == USER_ADDED_RESULT) {
            startupActivity.showToolbar()
            val bundle = bundleOf("defaultName" to facebookNameResponse.result, "defaultUsername" to response.result.username)
            findNavController().navigate(R.id.action_loggedInFragment_to_addFacebookFriendsAsFollowersStartupFragment, bundle)
        }

    }


    private suspend fun resetDatabases(){
        withContext(Dispatchers.IO) {
            followingUserCacheManager.loadFromScratch()
            notificationsManager.deleteAllNotifications()
            followingBattlesFeedCacheManager.deleteBattles()
            allBattlesCacheManager.deleteBattles()
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        if (::job.isInitialized) { job.cancel()}
    }



}

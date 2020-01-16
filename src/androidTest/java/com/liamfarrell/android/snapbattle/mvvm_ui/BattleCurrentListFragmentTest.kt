package com.liamfarrell.android.snapbattle.mvvm_ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.android.example.github.util.DataBindingIdlingResourceRule
import com.android.example.github.util.TaskExecutorWithIdlingResourceRule
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.testing.SingleFragmentActivity
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Voting
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment
import com.liamfarrell.android.snapbattle.util.EspressoTestUtil
import com.liamfarrell.android.snapbattle.util.RecyclerViewMatcher
import com.liamfarrell.android.snapbattle.util.ViewModelUtil
import com.liamfarrell.android.snapbattle.util.mock
import com.liamfarrell.android.snapbattle.viewmodels.CurrentBattlesViewModel
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class BattleCurrentListFragmentTest {
    @Rule
    @JvmField
    val activityRule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)

    @Rule
    @JvmField
    val executorRule = TaskExecutorWithIdlingResourceRule()


    @Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule(activityRule)
    private lateinit var viewModel: CurrentBattlesViewModel
    private val battles = MediatorLiveData<List<Battle>>()
    private val errorMessage = MediatorLiveData<String>()
    private val spinnerMutable = MutableLiveData<Boolean>()
    private val spinner : LiveData<Boolean> = spinnerMutable
    private lateinit var currentFragment : TestCurrentFragment


    @Before
    fun init() {
        viewModel = mock(CurrentBattlesViewModel::class.java)
        currentFragment = TestCurrentFragment()
        val viewModelSpy = spy(currentFragment)
        doReturn("13123111").`when`(viewModelSpy).getCognitoId()
        `when`(viewModel.battles).thenReturn(battles)
        `when`(viewModel.errorMessage).thenReturn(errorMessage)
        `when`(viewModel.spinner).thenReturn(spinner)
        val awsMobileClient = mock(AWSMobileClient::class.java)
        `when`(awsMobileClient.identityId).thenReturn("13123111")
        currentFragment.viewModelFactory = ViewModelUtil.createFor(viewModel)
        currentFragment.awsMobileClient = awsMobileClient
        activityRule.activity.setFragment(currentFragment)
        activityRule.runOnUiThread {
            currentFragment.binding.recyclerView.itemAnimator = null
        }
        EspressoTestUtil.disableProgressBarAnimations(activityRule)
    }

    @Test
    fun displayBattles() {
        spinnerMutable.postValue(true)
        onView(withId(R.id.progressContainer)).check(matches(isDisplayed()))
        val currentBattles = getCurrentBattles()
        battles.postValue(currentBattles)
        spinnerMutable.postValue(false)
        currentBattles.forEachIndexed { index, b ->
            onView(listMatcher().atPosition(index)).check(matches(hasDescendant(withText(b.battleName + " Battle"))))
            onView(listMatcher().atPosition(index)).check(matches(hasDescendant(withText(b.rounds.toString() + " Rounds"))))
            onView(listMatcher().atPosition(index)).check(matches(hasDescendant(withText("vs " + b.getOpponentName("13123111")))))
        }
        onView(withId(R.id.progressContainer)).check(matches(not(isDisplayed())))
    }

    @Test
    fun displayError() {
        spinnerMutable.postValue(true)
        onView(withId(R.id.progressContainer)).check(matches(isDisplayed()))
        errorMessage.postValue("error test")
        spinnerMutable.postValue(false)
        onView(listMatcher().atPosition(0)).check(doesNotExist())
        onView(withId(R.id.progressContainer)).check(matches(not(isDisplayed())))
        //Check toast is displayed
        onView(withText("error test"))
                .inRoot(withDecorView(not(activityRule.activity.window.decorView)))
                .check(matches(isDisplayed()));
    }

    @Test
    fun navigateToUsersBattles() {
        battles.postValue(listOf(Battle(2, "111", "222", "foo", 3).apply {videosUploaded = 3;  profilePicSmallSignedUrl = "333"; challengerProfilePicCount = 0; challengedProfilePicCount = 0 }))
        onView(withText("foo Battle")).perform(click())
        verify(currentFragment.navController).navigate(
                BattleCurrentListFragmentDirections.actionBattleCurrentListFragmentToViewBattleFragment(2)
        )
    }


    private fun getCurrentBattles() : List<Battle> {
        val battle1 = Battle(22, "13123111", "34324", "Sword Fight", 5)
                .apply {
            videosUploaded = 3
            challengerUsername = "freddie"
            challengedUsername = "sam"
            voting = Voting(ChooseVotingFragment.VotingChoice.PUBLIC, ChooseVotingFragment.VotingLength.ONE_MONTH, null, null, null)
        }
        val battle2 = Battle(26, "455555", "13123111", "Rap", 5)
                .apply {
            videosUploaded = 1
            challengerUsername = "joe"
            challengedUsername = "freddie"
            voting = Voting(ChooseVotingFragment.VotingChoice.MUTUAL_FACEBOOK, ChooseVotingFragment.VotingLength.TWENTY_FOUR_HOURS, null, null, null)
        }
        val battle3 = Battle(23, "13123111", "34324", "Joke", 5)
                .apply {
            videosUploaded = 2
            challengerUsername = "freddie"
            challengedUsername = "jason"
            voting = Voting(ChooseVotingFragment.VotingChoice.NONE, null, null, null, null)
        }
        return listOf(battle1, battle2, battle3)

    }


    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.recycler_view)
    }

    class TestCurrentFragment : BattleCurrentListFragment() {
        val navController = mock<NavController>()
        override fun navController() = navController
    }
}
package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import android.content.Context
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.liamfarrell.android.snapbattle.data.CurrentBattlesRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import org.mockito.Mockito.`when`
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse
import com.liamfarrell.android.snapbattle.util.CustomError
import com.liamfarrell.android.snapbattle.util.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.mockito.Mockito.*
import org.hamcrest.CoreMatchers.`is`

@RunWith(JUnit4::class)
class CurrentBattlesViewModelTest{

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    private val repository = mock(CurrentBattlesRepository::class.java)
    private val otherUsersProfilePicRepository = mock(OtherUsersProfilePicUrlRepository::class.java)
    private lateinit var viewModel: CurrentBattlesViewModel

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        // provide the scope explicitly, in this example using a constructor parameter
        Dispatchers.setMain(testDispatcher)
    }

    @ExperimentalCoroutinesApi
    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }


    @ExperimentalCoroutinesApi
    @Test
    fun currentBattlesTest() {
        testDispatcher.runBlockingTest  {
            val context = mock(Application::class.java)
            val awsMobileClient = mock<AWSMobileClient>()
            `when`(awsMobileClient.identityId).thenReturn("111")
            `when`(otherUsersProfilePicRepository.getOrUpdateProfilePicSignedUrl(anyString(), anyInt(), anyString())).thenReturn("")
            val battleList = mutableListOf<Battle>()
            battleList.add(Battle(2, "111", "222", "foo", 3).apply { profilePicSmallSignedUrl = "333"; challengerProfilePicCount = 0; challengedProfilePicCount = 0 })
            battleList.add(Battle(2, "333", "111", "fff", 1).apply { profilePicSmallSignedUrl = "333"; challengerProfilePicCount = 0; challengedProfilePicCount = 0  })
            battleList.add(Battle(2, "444", "111", "ggg", 2).apply {profilePicSmallSignedUrl = "333";  challengerProfilePicCount = 0; challengedProfilePicCount = 0 })
            val sqlResult = CurrentBattleResponse().apply { sqlResult = battleList }
            val response = AsyncTaskResult<CurrentBattleResponse>(sqlResult)
            `when`(repository.getCurrentBattles()).thenReturn(response)
            viewModel = CurrentBattlesViewModel(context, awsMobileClient, repository, otherUsersProfilePicRepository)
            viewModel.battles.observeForever(mock())
            verify(repository).getCurrentBattles()
            assertThat(viewModel.battles.value, `is`(battleList.toList()))
        }
    }



    @ExperimentalCoroutinesApi
    @Test
    fun errorTest() {
        testDispatcher.runBlockingTest  {
            val context = mock(Application::class.java)
            `when`(context.applicationContext).thenReturn(mock())
            val awsMobileClient = mock<AWSMobileClient>()
            `when`(awsMobileClient.identityId).thenReturn("111")
            val errorResult = object : CustomError(){
                override fun getErrorToastMessage(context: Context): String {
                    return "error test"
                }
            }
            val response = AsyncTaskResult<CurrentBattleResponse>(errorResult)
            `when`(repository.getCurrentBattles()).thenReturn(response)
            viewModel = CurrentBattlesViewModel(context, awsMobileClient, repository, otherUsersProfilePicRepository)
            viewModel.errorMessage.observeForever(mock())
            verify(repository).getCurrentBattles()
            verify(otherUsersProfilePicRepository, never()).getOrUpdateProfilePicSignedUrl(anyString(), anyInt(), anyString())
            assertThat(viewModel.errorMessage.value, `is`("error test"))
        }
    }


}
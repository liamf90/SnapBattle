package com.liamfarrell.android.snapbattle.viewmodels

import android.app.Application
import android.content.Context
import android.os.AsyncTask
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.amazonaws.auth.policy.Resource
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.liamfarrell.android.snapbattle.data.BattleNameSearchRepository
import com.liamfarrell.android.snapbattle.data.CurrentBattlesRepository
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.BattleTypeSuggestionsSearchResponse
import org.mockito.Mockito.`when`
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.CurrentBattleResponse
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.SuggestionsResponse
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.mockito.Mockito.*

@RunWith(JUnit4::class)
class BattleNameSearchViewModelTest{

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()



    private val battleNameSearchRepository = mock(BattleNameSearchRepository::class.java)
    private lateinit var viewModel: BattleNameSearchViewModel

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
    fun searchTest() {
        testDispatcher.runBlockingTest  {
            val context = mock(Application::class.java)
            `when`(battleNameSearchRepository.searchBattleName("skate")).thenReturn(mock())
            viewModel = BattleNameSearchViewModel(context, battleNameSearchRepository)
            viewModel.setSearchQueryText("skate")
            viewModel.searchBattle("skate")
            verify(battleNameSearchRepository).searchBattleName("skate")
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun queryDoesNotMatchNoSearchTest() {
        testDispatcher.runBlockingTest  {
            val context = mock(Application::class.java)
            `when`(battleNameSearchRepository.searchBattleName("skate")).thenReturn(mock())
            viewModel = BattleNameSearchViewModel(context, battleNameSearchRepository)
            viewModel.setSearchQueryText("skate2")
            viewModel.searchBattle("skate")
            verify(battleNameSearchRepository, never()).searchBattleName("skate")
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun resultClearedWhenQueryChangedToEmpty() {
        testDispatcher.runBlockingTest  {
            val context = mock(Application::class.java)
            val battleNameSuggestionList = listOf(SuggestionsResponse())
            val serverResponse = AsyncTaskResult<BattleTypeSuggestionsSearchResponse>(BattleTypeSuggestionsSearchResponse().apply { sqlResult = battleNameSuggestionList })
            `when`(battleNameSearchRepository.searchBattleName("skate")).thenReturn(serverResponse)
            val observer = mock<Observer<List<SuggestionsResponse>>>()
            viewModel = BattleNameSearchViewModel(context, battleNameSearchRepository)
            viewModel.searchResult.observeForever(observer)
            viewModel.setSearchQueryText("skate")
            viewModel.searchBattle("skate")
            verify(observer).onChanged(battleNameSuggestionList)
            viewModel.searchBattle("")
            verify(observer).onChanged(null)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun errorShownTest() {
        testDispatcher.runBlockingTest  {
            val context = mock(Application::class.java)
            val errorMessage = "This is a custom error test"
            val serverResponse = AsyncTaskResult<BattleTypeSuggestionsSearchResponse>(object : CustomError() {
                override fun getErrorToastMessage(context: Context): String {
                     return errorMessage
                }
            })
            `when`(battleNameSearchRepository.searchBattleName("skate")).thenReturn(serverResponse)
            viewModel = BattleNameSearchViewModel(context, battleNameSearchRepository)
            val observer = mock<Observer<in String?>>()
            viewModel.errorMessage.observeForever(observer)
            viewModel.setSearchQueryText("skate")
            viewModel.searchBattle("skate")
            verify(observer).onChanged("This is a custom error test")
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun searchResultShownTest() {
        testDispatcher.runBlockingTest  {
            val context = mock(Application::class.java)
            val battleNameSuggestionList = listOf(SuggestionsResponse())
            val serverResponse = AsyncTaskResult<BattleTypeSuggestionsSearchResponse>(BattleTypeSuggestionsSearchResponse().apply { sqlResult = battleNameSuggestionList })
            `when`(battleNameSearchRepository.searchBattleName("skate")).thenReturn(serverResponse)
            viewModel = BattleNameSearchViewModel(context, battleNameSearchRepository)
            val observer = mock<Observer<List<SuggestionsResponse>>>()
            viewModel.searchResult.observeForever(observer)
            viewModel.setSearchQueryText("skate")
            viewModel.searchBattle("skate")
            verify(observer).onChanged(battleNameSuggestionList)
        }
    }


}
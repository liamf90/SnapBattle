package com.liamfarrell.android.snapbattle.db

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.TimeUnit

abstract class DbTest {
    @get:Rule
    var countingTaskExecutorRule = CountingTaskExecutorRule()

    private lateinit var _db: SnapBattleDatabase

    @ExperimentalCoroutinesApi
    protected val testDispatcher = TestCoroutineDispatcher()

    val db: SnapBattleDatabase
        get() = _db

    @Before
    fun initDb() {
        _db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                SnapBattleDatabase::class.java
        ).build()
    }

    @ExperimentalCoroutinesApi
    @Before
    fun setupCoroutine() {
        // provide the scope explicitly, in this example using a constructor parameter
        Dispatchers.setMain(testDispatcher)
    }


    @After
    fun closeDb() {
        countingTaskExecutorRule.drainTasks(10, TimeUnit.SECONDS)
        _db.close()
    }


    @ExperimentalCoroutinesApi
    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

}
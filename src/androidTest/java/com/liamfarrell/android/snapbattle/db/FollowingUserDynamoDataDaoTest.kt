package com.liamfarrell.android.snapbattle.db

import org.mockito.ArgumentMatchers.isNotNull
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import com.liamfarrell.android.snapbattle.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.junit.Rule
import org.junit.Test

class FollowingUserDynamoDataDaoTest : DbTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Test
    fun insertAndRead() {
        testDispatcher.runBlockingTest {
            db.followingUserDynamoCountDao().insert(FollowingUserDynamoCount(4))
            val followingUserDynamoCount = db.followingUserDynamoCountDao().getDynamoCount()
            assertThat(followingUserDynamoCount, `is`(4))
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun dynamoCountChanged() {
        testDispatcher.runBlockingTest {
            db.followingUserDynamoCountDao().insert(FollowingUserDynamoCount(4))
            val followingUserDynamoCount = db.followingUserDynamoCountDao().getDynamoCount()
            assertThat(followingUserDynamoCount, `is`(4))
            db.followingUserDynamoCountDao().updateFollowingUserDynamoCount(5)
            val followingUserDynamoCount2 = db.followingUserDynamoCountDao().getDynamoCount()
            assertThat(followingUserDynamoCount2, `is`(5))
            db.followingUserDynamoCountDao().insert(FollowingUserDynamoCount(9))
            val followingUserDynamoCount3 = db.followingUserDynamoCountDao().getDynamoCount()
            assertThat(followingUserDynamoCount3, `is`(9))
        }
    }


}
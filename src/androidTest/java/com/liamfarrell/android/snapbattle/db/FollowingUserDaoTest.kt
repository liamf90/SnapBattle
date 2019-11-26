package com.liamfarrell.android.snapbattle.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import com.liamfarrell.android.snapbattle.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Rule
import org.junit.Test

class FollowingUserDaoTest : DbTest(){

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Test
    fun insertAndRead() {
        val followingUser = User("13", "joey", "Joe Burns", 5, null)
        testDispatcher.runBlockingTest{
            db.followingUserDao().insertAll(listOf(followingUser))
            val allFollowingUsers = db.followingUserDao().getAllFollowingUsers()
            val user = allFollowingUsers.get(0)
            assertThat(allFollowingUsers.size,`is`(1))
            assertThat(user.cognitoId, `is`("13"))
            assertThat(user.username, `is`("joey"))
            assertThat(user.facebookName, `is`("Joe Burns"))
            assertThat(user.profilePicCount, `is`(5))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun insertAndSearch() {
        val followingUsers = listOf(
            User("13", "joey", "Joe Burns", 5, null),
                User("15", "hesssy.sass", "Jim Johnson", 0, null),
                User("10", "greg", "Greg James", 0, null),
                User("199", "bob", "Bobby Holmes", 0, null),
                User("800", "sammy", "Samantha Hall", 0, null)
        )
        testDispatcher.runBlockingTest{
            db.followingUserDao().insertAll(followingUsers)
            var searchResult = db.followingUserDao().searchUsersInCache("Samantha")
            assertThat(searchResult.size,`is`(1))
            assertThat(searchResult.get(0).cognitoId, `is`("800"))

            searchResult = db.followingUserDao().searchUsersInCache("hesssy.sass")
            assertThat(searchResult.size,`is`(1))
            assertThat(searchResult.get(0).cognitoId, `is`("15"))

            searchResult = db.followingUserDao().searchUsersInCache("GREG")
            assertThat(searchResult.size,`is`(1))
            assertThat(searchResult.get(0).cognitoId, `is`("10"))

            searchResult = db.followingUserDao().searchUsersInCache("hol")
            assertThat(searchResult.size,`is`(1))
            assertThat(searchResult.get(0).cognitoId, `is`("199"))
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun insertAndDelete() {
        val followingUser = User("13", "joey", "Joe Burns", 5, null)
        testDispatcher.runBlockingTest{
            db.followingUserDao().insertAll(listOf(followingUser))
            db.followingUserDao().deleteFromUser("13")
            val allFollowingUsers = db.followingUserDao().getAllFollowingUsers()
            assertThat(allFollowingUsers.size,`is`(0))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun updateUsername() {
        val followingUser = User("13", "joey", "Joe Burns", 5, null)
        testDispatcher.runBlockingTest{
            db.followingUserDao().insertAll(listOf(followingUser))
            db.followingUserDao().updateUsername("13", "tommy")
            val allUsers = db.followingUserDao().getAllFollowingUsers()
            assertThat(allUsers.size,`is`(1))
            assertThat(allUsers.get(0).cognitoId,`is`("13"))
            assertThat(allUsers.get(0).username,`is`("tommy"))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun updateName() {
        val followingUser = User("13", "joey", "Joe Burns", 5, null)
        testDispatcher.runBlockingTest{
            db.followingUserDao().insertAll(listOf(followingUser))
            db.followingUserDao().updateName("13", "clyde")
            val allUsers = db.followingUserDao().getAllFollowingUsers()
            assertThat(allUsers.size,`is`(1))
            assertThat(allUsers.get(0).cognitoId,`is`("13"))
            assertThat(allUsers.get(0).facebookName,`is`("clyde"))
        }
    }



}
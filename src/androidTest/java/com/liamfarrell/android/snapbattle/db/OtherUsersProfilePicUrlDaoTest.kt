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

class OtherUsersProfilePicUrlDaoTest : DbTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Test
    fun insertAndRead() {
        val signedUrlUser = OtherUsersProfilePicUrlCache("34", 5, "hello.jpg")
        testDispatcher.runBlockingTest {
            db.otherUsersProfilePicCacheDao().insertSignedUrl(signedUrlUser)
            val otherUsersProfilePicUser = db.otherUsersProfilePicCacheDao().getSignedUrlAndProfilePicForUser("34")
            assertThat(otherUsersProfilePicUser, notNullValue())
            assertThat(otherUsersProfilePicUser?.cognito_id, `is`("34"))
            assertThat(otherUsersProfilePicUser?.last_saved_signed_url, `is`("hello.jpg"))
            assertThat(otherUsersProfilePicUser?.profile_pic_count, `is`(5))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun insertAndUpdate() {
        val signedUrlUser = OtherUsersProfilePicUrlCache("34", 5, "hello.jpg")
        testDispatcher.runBlockingTest {
            db.otherUsersProfilePicCacheDao().insertSignedUrl(signedUrlUser)
            val otherUsersProfilePicUser = db.otherUsersProfilePicCacheDao().getSignedUrlAndProfilePicForUser("34")
            val signedUrlUserUpdated = OtherUsersProfilePicUrlCache("34", 6, "hello2.jpg")
            db.otherUsersProfilePicCacheDao().insertSignedUrl(signedUrlUserUpdated)
            val otherUsersProfilePicUser2 = db.otherUsersProfilePicCacheDao().getSignedUrlAndProfilePicForUser("34")
            assertThat(otherUsersProfilePicUser2, notNullValue())
            assertThat(otherUsersProfilePicUser2?.cognito_id, `is`("34"))
            assertThat(otherUsersProfilePicUser2?.last_saved_signed_url, `is`("hello2.jpg"))
            assertThat(otherUsersProfilePicUser2?.profile_pic_count, `is`(6))

        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun insertAndDelete() {
        val signedUrlUser = OtherUsersProfilePicUrlCache("34", 5, "hello.jpg")
        testDispatcher.runBlockingTest {
            db.otherUsersProfilePicCacheDao().insertSignedUrl(signedUrlUser)
            val otherUsersProfilePicUser = db.otherUsersProfilePicCacheDao().getSignedUrlAndProfilePicForUser("34")
            assertThat(otherUsersProfilePicUser, notNullValue())
            db.otherUsersProfilePicCacheDao().deleteAllProfilePicSignedUrls()
            val otherUsersProfilePicUser2 = db.otherUsersProfilePicCacheDao().getSignedUrlAndProfilePicForUser("34")
            assertThat(otherUsersProfilePicUser2, nullValue())
        }
    }

}
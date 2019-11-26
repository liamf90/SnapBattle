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

class ThumbnailSignedUrlDaoTest : DbTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Test
    fun insertAndRead() {
        val thumbnailSignedUrl = ThumbnailSignedUrlCache (55, "signedurl1.jpg")
        testDispatcher.runBlockingTest {
            db.thumbnailSignedUrlDao().insertSignedUrl(thumbnailSignedUrl)
            val thumbnailSignedUrlFromDb = db.thumbnailSignedUrlDao().getLastSavedThumbnailSignedUrl(55)
            assertThat(thumbnailSignedUrlFromDb, notNullValue())
            assertThat(thumbnailSignedUrlFromDb, `is`("signedurl1.jpg"))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun doesNotExist() {
        val thumbnailSignedUrl = ThumbnailSignedUrlCache (99, "signedurl1.jpg")
        testDispatcher.runBlockingTest {
            db.thumbnailSignedUrlDao().insertSignedUrl(thumbnailSignedUrl)
            val thumbnailSignedUrlFromDb  = db.thumbnailSignedUrlDao().getLastSavedThumbnailSignedUrl(55)
            assertThat(thumbnailSignedUrlFromDb, nullValue())
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun urlChanged() {
        val thumbnailSignedUrl = ThumbnailSignedUrlCache (99, "signedurl1.jpg")
        val thumbnailSignedUrl2 = ThumbnailSignedUrlCache (99, "signedurl2.jpg")
        testDispatcher.runBlockingTest {
            db.thumbnailSignedUrlDao().insertSignedUrl(thumbnailSignedUrl)
            val thumbnailSignedUrlFromDb  = db.thumbnailSignedUrlDao().getLastSavedThumbnailSignedUrl(99)
            assertThat(thumbnailSignedUrlFromDb, `is`("signedurl1.jpg"))
            db.thumbnailSignedUrlDao().insertSignedUrl(thumbnailSignedUrl2)
            val thumbnailSignedUrlFromDb2  = db.thumbnailSignedUrlDao().getLastSavedThumbnailSignedUrl(99)
            assertThat(thumbnailSignedUrlFromDb2, `is`("signedurl2.jpg"))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun deleteAll() {
        val thumbnailSignedUrl = ThumbnailSignedUrlCache (99, "signedurl1.jpg")
        val thumbnailSignedUrl2 = ThumbnailSignedUrlCache (100, "signedurl2.jpg")
        testDispatcher.runBlockingTest {
            db.thumbnailSignedUrlDao().insertSignedUrl(thumbnailSignedUrl)
            db.thumbnailSignedUrlDao().insertSignedUrl(thumbnailSignedUrl2)
            db.thumbnailSignedUrlDao().deleteAllProfilePicSignedUrls()
            val thumbnailSignedUrlFromDb1  = db.thumbnailSignedUrlDao().getLastSavedThumbnailSignedUrl(99)
            val thumbnailSignedUrlFromDb2  = db.thumbnailSignedUrlDao().getLastSavedThumbnailSignedUrl(100)
            assertThat(thumbnailSignedUrlFromDb1, nullValue())
            assertThat(thumbnailSignedUrlFromDb2, nullValue())
        }
    }

}
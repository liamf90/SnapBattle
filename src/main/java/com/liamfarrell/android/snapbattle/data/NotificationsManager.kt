package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.db.NotificationDao
import com.liamfarrell.android.snapbattle.db.NotificationDynamoInfoDao
import com.liamfarrell.android.snapbattle.notifications.NotificationDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsManager @Inject constructor(
        private val notificationsDynamoDbRepository: NotificationsDynamoDbRepository,
        private val notificationsDynamoInfoDao : NotificationDynamoInfoDao,
        private val notificationsDao: NotificationDao)
{

    val noMoreNotifications = MutableLiveData<Boolean>()
    val loadingMoreNotifications = MutableLiveData<Boolean>()
    val loadingNewNotifications = MutableLiveData<Boolean>()


    init{
        noMoreNotifications.value = false
        loadingMoreNotifications.value = false
        loadingNewNotifications.value = false
    }

    suspend fun checkHasAllNotificationsBeenSeen(){
        notificationsDynamoInfoDao.updateHasAllNotificationsBeenSeen(notificationsDynamoDbRepository.getDynamoHasSeenAllNotifications())
    }

    suspend fun updateAllNotificationsHaveBeenSeen(){
        notificationsDynamoInfoDao.updateHasAllNotificationsBeenSeen(true)
        notificationsDynamoDbRepository.updateDynamoSeenAllNotifications()
    }

    suspend fun requestMoreBattles() {
        loadingMoreNotifications.postValue(true)
        noMoreNotifications.postValue(false)

        val notificationCount = notificationsDao.getCountAllNotifications()
        val totalNotificationsCountDynamo = notificationsDynamoDbRepository.getNotificationCountDynamo()
        val lastAllNotificationDynamoCount = notificationsDynamoInfoDao.getNotificationDynamoCount()
            if (totalNotificationsCountDynamo != notificationCount) {
            val startIndex = totalNotificationsCountDynamo - lastAllNotificationDynamoCount + notificationCount
            val endIndex = startIndex + NETWORK_PAGE_SIZE - 1

            val moreNotificationsList = notificationsDynamoDbRepository.getNotificationListFromDynamo(startIndex, endIndex)
            notificationsDao.insertAll(moreNotificationsList.map{NotificationDb(it)})
            notificationsDynamoInfoDao.updateNotificationsDynamoCount(totalNotificationsCountDynamo)
        } else {
            noMoreNotifications.postValue(true)
        }
        loadingMoreNotifications.postValue(false)
    }


    suspend fun checkForUpdates() {
        val notificationCountDynamo = notificationsDynamoDbRepository.getNotificationCountDynamo()
        val lastNotificationsDynamoCount = notificationsDynamoInfoDao.getNotificationDynamoCount()

        if (notificationCountDynamo == 0) {
            noMoreNotifications.postValue(true)
            return
        }


        //if there is more topBattles to be loaded from server to cache, get new ones then update old ones, else just update old ones
        if (notificationCountDynamo != lastNotificationsDynamoCount) {
            loadingNewNotifications.postValue(true)

            // new topBattles list
            val startIndex = 0

            val endIndex = if (lastNotificationsDynamoCount == 0){
                //initial load, just download to the database trim size
                DATABASE_TRIM_SIZE - 1
            } else {
                notificationCountDynamo - lastNotificationsDynamoCount - 1
            }

            val newNotificationsList = notificationsDynamoDbRepository.getNotificationListFromDynamo(startIndex, endIndex)
            notificationsDao.insertNotifications(newNotificationsList.map { NotificationDb(it) },notificationCountDynamo)
        }
        loadingNewNotifications.postValue(false)
    }

    suspend fun deleteAllNotifications(){
        withContext(Dispatchers.IO) {
            notificationsDao.deleteAllNotifications()
        }
    }



    companion object {
        private const val NETWORK_PAGE_SIZE = 5
        const val DATABASE_TRIM_SIZE = 10
    }
}


package com.liamfarrell.android.snapbattle.data.following_battle_feed

import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.data.BattlesRepository
import com.liamfarrell.android.snapbattle.db.BattleDao
import com.liamfarrell.android.snapbattle.db.following_battle_feed.FollowingBattlesFeedDynamoDataDao
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingBattlesFeedCacheManager @Inject constructor(
        private val followingBattlesFeedDynamoInfoDao : FollowingBattlesFeedDynamoDataDao,
        private val battleDao: BattleDao,
        private val followingBattlesFeedDynamoRepository: FollowingBattlesFeedDynamodbRepository,
        private val battlesApi : BattlesRepository)
{
    val noMoreBattles = MutableLiveData<Boolean>()
    val loadingMoreBattles = MutableLiveData<Boolean>()
    val loadingNewBattles = MutableLiveData<Boolean>()


    init{
        noMoreBattles.value = false
        loadingMoreBattles.value = false
        loadingNewBattles.value = false
    }


    suspend fun requestMoreBattles() {
        loadingMoreBattles.postValue(true)
        noMoreBattles.postValue(false)

         val battleCount = battleDao.getCountAllBattles()
         val totalBattlesCountDynamo = followingBattlesFeedDynamoRepository.getBattlesCountDynamo()
         val lastAllBattleDynamoCount = followingBattlesFeedDynamoInfoDao.getDynamoCount()


        if (totalBattlesCountDynamo != battleCount) {
            val startIndex = totalBattlesCountDynamo - lastAllBattleDynamoCount + battleCount
            val endIndex = startIndex + NETWORK_PAGE_SIZE - 1

            val moreBattlesIDList = followingBattlesFeedDynamoRepository.loadListFromDynamo(startIndex, endIndex)
            val moreBattlesResponse = battlesApi.getFriendsBattles(moreBattlesIDList)
            if (moreBattlesResponse.error == null) {
                if (moreBattlesResponse.result.sqlResult.size != NETWORK_PAGE_SIZE) {
                    noMoreBattles.postValue(true)
                }
                battleDao.insertAll(moreBattlesResponse.result.sqlResult)
            } else{
                //ERROR
            }

        } else {
            noMoreBattles.postValue(true)
        }
         loadingMoreBattles.postValue(false)
    }


   suspend fun checkForUpdates() {
        val battleCountDynamo = followingBattlesFeedDynamoRepository.getBattlesCountDynamo()
        val lastAllBattlesDynamoCount = followingBattlesFeedDynamoInfoDao.getDynamoCount()
         val fullFeedUpdateDynamoCount = followingBattlesFeedDynamoRepository.getFullFeedUpdateCount()
         val lastfullFeedUpdateCount = followingBattlesFeedDynamoInfoDao.getLastFullFeedUpdateDynamoCount()

        if (fullFeedUpdateDynamoCount != lastfullFeedUpdateCount){
            //feed order has been changed around. clear database, then re-add battles
            followingBattlesFeedDynamoInfoDao.resetFollowingBattlesInfo()
            battleDao.deleteAllBattles()
            checkForUpdates()
            return
        } else {

            //if there is more battles to be loaded from server to cache, get new ones then update old ones, else just update old ones
            if (battleCountDynamo == lastAllBattlesDynamoCount) {
                val oldBattlesList = battleDao.getAllBattleIDs()
                // Update old battles
                updateBattles(oldBattlesList)


            } else {
                loadingNewBattles.postValue(true)

                // to update list = old battles minus new battles
                val oldBattlesList = battleDao.getAllBattleIDs()

                // new battles list
                val startIndex = 0

                val endIndex = if (lastAllBattlesDynamoCount == 0) {
                    //initial load, just download to the database trim size
                    DATABASE_TRIM_SIZE - 1
                } else {
                    battleCountDynamo - lastAllBattlesDynamoCount - 1
                }

                val newBattlesList = followingBattlesFeedDynamoRepository.loadListFromDynamo(startIndex, endIndex)
                val moreBattlesResponse = battlesApi.getFriendsBattles(newBattlesList)
                if (moreBattlesResponse.error == null) {
                    if (moreBattlesResponse.result.sqlResult.size != NETWORK_PAGE_SIZE) {
                        noMoreBattles.postValue(true)
                    }

                    battleDao.insertAll(moreBattlesResponse.result.sqlResult)
                    followingBattlesFeedDynamoInfoDao.updateFollowingBattlesDynamoCount(battleCountDynamo)

                } else {
                    //ERROR
                }

                // Update old battles
                updateBattles(oldBattlesList)
            }
        }

        loadingNewBattles.postValue(false)
    }


     private suspend fun updateBattles(battlesIDsToUpdate : List<Int>) {
        if (battlesIDsToUpdate.size > 0) {
            val lastTimeAllBattlesUpdate = followingBattlesFeedDynamoInfoDao.getLastTimeBattlesUpdated()

            val resultList = lastTimeAllBattlesUpdate?.let {
                battlesApi.getFriendsBattles(battlesIDsToUpdate, lastTimeAllBattlesUpdate)
            } ?:   battlesApi.getFriendsBattles(battlesIDsToUpdate)

            if (resultList.error == null) {
                battleDao.insertAll(resultList.result.sqlResult)
                //update last update time
                followingBattlesFeedDynamoInfoDao.updateLastTimeBattlesUpdated(Calendar.getInstance().time)

            } else{
                //ERROR
            }
        }
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 5
        const val DATABASE_TRIM_SIZE = 10
    }



}


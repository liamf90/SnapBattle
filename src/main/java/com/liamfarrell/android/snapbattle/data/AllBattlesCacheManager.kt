package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.db.AllBattlesDynamoDataDao
import com.liamfarrell.android.snapbattle.db.BattleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllBattlesCacheManager @Inject constructor(
        private val allBattlesDynamoInfoDao : AllBattlesDynamoDataDao,
        private val battleDao: BattleDao,
        private val allBattlesDynamoRepository : AllBattlesFeedDynamodbRepository,
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
         val totalBattlesCountDynamo = allBattlesDynamoRepository.getBattlesCountDynamo()
         val lastAllBattleDynamoCount = allBattlesDynamoInfoDao.getDynamoCount()

        if (totalBattlesCountDynamo != battleCount) {
            val startIndex = totalBattlesCountDynamo - lastAllBattleDynamoCount + battleCount
            val endIndex = startIndex + NETWORK_PAGE_SIZE - 1

            val moreBattlesIDList = allBattlesDynamoRepository.loadListFromDynamo(startIndex, endIndex)
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
        val battleCountDynamo = allBattlesDynamoRepository.getBattlesCountDynamo()
        val lastAllBattlesDynamoCount = allBattlesDynamoInfoDao.getDynamoCount()

       if (battleCountDynamo == 0) {
           noMoreBattles.postValue(true)
           return
       }

       //if there is more topBattles to be loaded from server to cache, get new ones then update old ones, else just update old ones
        if (battleCountDynamo == lastAllBattlesDynamoCount) {
                val oldBattlesList =  battleDao.getAllBattleIDs()
                // Update old topBattles
                updateBattles(oldBattlesList)


        } else {
            loadingNewBattles.postValue(true)

            // to update list = old topBattles minus new topBattles
            val oldBattlesList = battleDao.getAllBattleIDs()

            // new topBattles list
            val startIndex = 0

            val endIndex = if (lastAllBattlesDynamoCount == 0){
                //initial load, just download to the database trim size
                DATABASE_TRIM_SIZE - 1
            } else {
                battleCountDynamo - lastAllBattlesDynamoCount - 1
            }

            val newBattlesList = allBattlesDynamoRepository.loadListFromDynamo(startIndex, endIndex)
            val moreBattlesResponse = battlesApi.getFriendsBattles(newBattlesList)
            if (moreBattlesResponse.error == null) {
                if (moreBattlesResponse.result.sqlResult.size != newBattlesList.size) {
                    noMoreBattles.postValue(true)
                }

                battleDao.insertAll(moreBattlesResponse.result.sqlResult)
                allBattlesDynamoInfoDao.updateAllBattlesDynamoCount(battleCountDynamo)

            } else{
                //ERROR
            }

            // Update old topBattles
            updateBattles(oldBattlesList)
        }

        loadingNewBattles.postValue(false)
    }


     private suspend fun updateBattles(battlesIDsToUpdate : List<Int>) {
        if (battlesIDsToUpdate.size > 0) {
            val lastTimeAllBattlesUpdate = allBattlesDynamoInfoDao.getLastTimeBattlesUpdated()

            val resultList = lastTimeAllBattlesUpdate?.let {
                battlesApi.getFriendsBattles(battlesIDsToUpdate, lastTimeAllBattlesUpdate)
            } ?:   battlesApi.getFriendsBattles(battlesIDsToUpdate)

            if (resultList.error == null) {
                battleDao.insertAll(resultList.result.sqlResult)
                //update last update time
                allBattlesDynamoInfoDao.updateLastTimeBattlesUpdated(Calendar.getInstance().time)

            } else{
                //ERROR
            }
        }
    }

    suspend fun deleteBattles(){
        withContext(Dispatchers.IO) {
            allBattlesDynamoInfoDao.insert(AllBattlesDynamoCount())
            battleDao.deleteAllBattles()
        }

    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 5
        const val DATABASE_TRIM_SIZE = 10
    }



}


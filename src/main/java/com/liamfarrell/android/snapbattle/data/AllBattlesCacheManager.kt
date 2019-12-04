package com.liamfarrell.android.snapbattle.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.db.AllBattlesDynamoDataDao
import com.liamfarrell.android.snapbattle.db.BattleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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


     fun requestMoreBattles() {
        loadingMoreBattles.postValue(true)
        noMoreBattles.postValue(false)

         val battleCount = battleDao.getCountAllBattles()
         val totalBattlesCountDynamo = allBattlesDynamoRepository.getBattlesCountDynamo()
         val lastAllBattleDynamoCount = allBattlesDynamoInfoDao.getDynamoCount()

        if (totalBattlesCountDynamo != battleCount) {
            val startIndex = totalBattlesCountDynamo - lastAllBattleDynamoCount + battleCount
            val endIndex = startIndex + NETWORK_PAGE_SIZE - 1

            val moreBattlesIDList = allBattlesDynamoRepository.loadListFromDynamo(startIndex, endIndex)

            val moreBattlesResponse = battlesApi.getFriendsBattlesSync(moreBattlesIDList)

            if (moreBattlesResponse.sqlResult.size != NETWORK_PAGE_SIZE) {
                noMoreBattles.postValue(true)
            }
            battleDao.insertAll(moreBattlesResponse.sqlResult)


        } else {
            noMoreBattles.postValue(true)
        }
         loadingMoreBattles.postValue(false)
    }


    fun checkForUpdates() {
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
            val moreBattlesResponse = battlesApi.getFriendsBattlesSync(newBattlesList)

            if (moreBattlesResponse.sqlResult.size != newBattlesList.size) {
                noMoreBattles.postValue(true)
            }

            battleDao.insertAll(moreBattlesResponse.sqlResult)
            allBattlesDynamoInfoDao.updateAllBattlesDynamoCount(battleCountDynamo)


            // Update old topBattles
            updateBattles(oldBattlesList)
        }

        loadingNewBattles.postValue(false)
    }


     private  fun updateBattles(battlesIDsToUpdate : List<Int>) {
        if (battlesIDsToUpdate.isNotEmpty()) {
            val lastTimeAllBattlesUpdate = allBattlesDynamoInfoDao.getLastTimeBattlesUpdated()

            val resultList = lastTimeAllBattlesUpdate?.let {
                battlesApi.getFriendsBattlesSync(battlesIDsToUpdate, lastTimeAllBattlesUpdate)
            } ?:   battlesApi.getFriendsBattlesSync(battlesIDsToUpdate)

            battleDao.insertAll(resultList.sqlResult)
            //update last update time
            allBattlesDynamoInfoDao.updateLastTimeBattlesUpdated(Calendar.getInstance().time)
        }
    }

    suspend fun deleteBattles(){
        withContext(Dispatchers.IO) {
            allBattlesDynamoInfoDao.insert(AllBattlesDynamoCount())
            battleDao.deleteAllBattles()
        }
    }

    fun increaseLikeCount(battleId: Int){
        GlobalScope.launch (Dispatchers.IO) {
            battleDao.increaseLikeCount (battleId)
        }
    }

    fun decreaseLikeCount(battleId: Int){
        GlobalScope.launch (Dispatchers.IO) {
            battleDao.decreaseLikeCount (battleId)
        }
    }

    fun increaseDislikeCount(battleId: Int){
        GlobalScope.launch (Dispatchers.IO) {
            battleDao.increaseDislikeCount (battleId)
        }
    }

    fun decreaseDislikeCount(battleId: Int){
        GlobalScope.launch (Dispatchers.IO) {
            battleDao.decreaseDislikeCount (battleId)
        }
    }

    fun setHasVoted(battleId: Int) {
        GlobalScope.launch (Dispatchers.IO) {
            battleDao.setHasVoted(battleId)
        }
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 5
        const val DATABASE_TRIM_SIZE = 10
    }





}


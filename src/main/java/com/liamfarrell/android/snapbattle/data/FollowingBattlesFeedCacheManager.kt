package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.MutableLiveData
import com.liamfarrell.android.snapbattle.db.*
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingBattlesFeedCacheManager @Inject constructor(
        private val followingBattlesFeedDynamoInfoDao : FollowingBattlesFeedDynamoDataDao,
        private val followingBattleDao: FollowingBattleDao,
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


     fun requestMoreBattles() {
        loadingMoreBattles.postValue(true)
        noMoreBattles.postValue(false)

         val battleCount = followingBattleDao.getCountAllBattles()
         val totalBattlesCountDynamo = followingBattlesFeedDynamoRepository.getBattlesCountDynamo()
         val lastAllBattleDynamoCount = followingBattlesFeedDynamoInfoDao.getDynamoCount()


        if (totalBattlesCountDynamo != battleCount) {
            val startIndex = totalBattlesCountDynamo - lastAllBattleDynamoCount + battleCount
            val endIndex = startIndex + NETWORK_PAGE_SIZE - 1

            val moreBattlesIDList = followingBattlesFeedDynamoRepository.loadListFromDynamo(startIndex, endIndex)
            val moreBattlesResponse = battlesApi.getFriendsBattlesSync(moreBattlesIDList)

            followingBattleDao.insertAll( moreBattlesResponse.sqlResult.map { FollowingBattle(it.battleId, battle = it)})

            if (moreBattlesResponse.sqlResult.size != NETWORK_PAGE_SIZE) {
                noMoreBattles.postValue(true)
            }



        } else {
            noMoreBattles.postValue(true)
        }
         loadingMoreBattles.postValue(false)
    }


    fun checkForUpdates() {
        val battleCountDynamo = followingBattlesFeedDynamoRepository.getBattlesCountDynamo()
        val lastAllBattlesDynamoCount = followingBattlesFeedDynamoInfoDao.getDynamoCount()
         val fullFeedUpdateDynamoCount = followingBattlesFeedDynamoRepository.getFullFeedUpdateCount()
         val lastFullFeedUpdateCount = followingBattlesFeedDynamoInfoDao.getLastFullFeedUpdateDynamoCount()

       if (battleCountDynamo == 0) {
           noMoreBattles.postValue(true)
           return
       }

        if (fullFeedUpdateDynamoCount != lastFullFeedUpdateCount){
            //feed order has been changed around. clear database, then re-add topBattles
            followingBattlesFeedDynamoInfoDao.insert(FollowingBattlesDynamoCount(full_feed_update_bound = fullFeedUpdateDynamoCount))
            followingBattleDao.deleteAllBattles()
            checkForUpdates()
            return
        } else {

            //if there is more topBattles to be loaded from server to cache, get new ones then update old ones, else just update old ones
            if (battleCountDynamo == lastAllBattlesDynamoCount) {
                val oldBattlesList = followingBattleDao.getAllBattleIDs()
                // Update old topBattles
                updateBattles(oldBattlesList)


            } else {
                loadingNewBattles.postValue(true)

                // to update list = old topBattles minus new topBattles
                val oldBattlesList = followingBattleDao.getAllBattleIDs()

                // new topBattles list
                val startIndex = 0

                val endIndex = if (lastAllBattlesDynamoCount == 0) {
                    //initial load, just download to the database trim size
                    DATABASE_TRIM_SIZE - 1
                } else {
                    battleCountDynamo - lastAllBattlesDynamoCount - 1
                }

                val newBattlesList = followingBattlesFeedDynamoRepository.loadListFromDynamo(startIndex, endIndex)
                val moreBattlesResponse = battlesApi.getFriendsBattlesSync(newBattlesList)

                followingBattleDao.insertAll(moreBattlesResponse.sqlResult.map { FollowingBattle(it.battleId, it) })
                followingBattlesFeedDynamoInfoDao.updateFollowingBattlesDynamoCount(battleCountDynamo)


                // Update old topBattles
                updateBattles(oldBattlesList)
            }
        }

        loadingNewBattles.postValue(false)
    }


     private  fun updateBattles(battlesIDsToUpdate : List<Int>) {
        if (battlesIDsToUpdate.isNotEmpty()) {
            val lastTimeAllBattlesUpdate = followingBattlesFeedDynamoInfoDao.getLastTimeBattlesUpdated()

            val resultList = lastTimeAllBattlesUpdate?.let {
                battlesApi.getFriendsBattlesSync(battlesIDsToUpdate, lastTimeAllBattlesUpdate)
            } ?:   battlesApi.getFriendsBattlesSync(battlesIDsToUpdate)


            followingBattleDao.insertAll(resultList.sqlResult.map { FollowingBattle(it.battleId, it) })
            //update last update time
            followingBattlesFeedDynamoInfoDao.updateLastTimeBattlesUpdated(Calendar.getInstance().time)


        }
    }

     suspend fun deleteBattles(){
         withContext(Dispatchers.IO) {
             followingBattlesFeedDynamoInfoDao.insert(FollowingBattlesDynamoCount())
             followingBattleDao.deleteAllBattles()
         }
     }

    fun increaseLikeCount(battleId: Int){
        followingBattleDao.increaseLikeCount (battleId).subscribeOn(Schedulers.single()).subscribe()

    }

    fun decreaseLikeCount(battleId: Int){
        followingBattleDao.decreaseLikeCount (battleId).subscribeOn(Schedulers.single()).subscribe()

    }

    fun increaseDislikeCount(battleId: Int){
         followingBattleDao.increaseDislikeCount (battleId).subscribeOn(Schedulers.single()).subscribe()
    }

    fun decreaseDislikeCount(battleId: Int){
         followingBattleDao.decreaseDislikeCount (battleId).subscribeOn(Schedulers.single()).subscribe()

    }

    fun setHasVoted(battleId: Int) {
         followingBattleDao.setHasVoted(battleId).subscribeOn(Schedulers.single()).subscribe()
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 5
        const val DATABASE_TRIM_SIZE = 10
    }



}


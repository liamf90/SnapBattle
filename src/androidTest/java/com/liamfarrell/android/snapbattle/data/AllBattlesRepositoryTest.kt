package com.liamfarrell.android.snapbattle.data


import android.util.Log
import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import com.android.example.github.util.TaskExecutorWithIdlingResourceRule
import com.liamfarrell.android.snapbattle.db.AllBattlesBattle
import com.liamfarrell.android.snapbattle.db.DbTest
import com.liamfarrell.android.snapbattle.db.ThumbnailSignedUrlCache
import com.liamfarrell.android.snapbattle.model.*
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetFriendsBattlesResponse
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment
import com.liamfarrell.android.snapbattle.util.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.SECONDS



class AllBattlesRepositoryTest : DbTest(){

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val executorRule = TaskExecutorWithIdlingResourceRule()

    lateinit var repository: AllBattlesRepository
    lateinit var battlesApi: BattlesRepository
    lateinit var allBattlesDynamoDbRepository : AllBattlesFeedDynamodbRepository
    lateinit var allBattlesCacheManager : AllBattlesCacheManager


    @ExperimentalCoroutinesApi
    @Before
    fun initialiseRepository() {
        testDispatcher.runBlockingTest {
            allBattlesDynamoDbRepository = mock(AllBattlesFeedDynamodbRepository::class.java)
            battlesApi = mock(BattlesRepository::class.java)
            allBattlesCacheManager = AllBattlesCacheManager(db.allBattlesDynamoDataDao(), db.battlesDao(), allBattlesDynamoDbRepository, battlesApi )
            repository = AllBattlesRepository(allBattlesCacheManager, db.battlesDao())
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun insertAndReadNoNetwork() {
        testDispatcher.runBlockingTest {
            val battles = getBattlesResponse(40)
            `when`(battlesApi.getFriendsBattles((1..40).toList())).thenReturn(AsyncTaskResult(GetFriendsBattlesResponse().apply { sqlResult = battles.map { it.battle } }))
            `when`(allBattlesDynamoDbRepository.getBattlesCountDynamo()).thenReturn(40)
            db.battlesDao().insertAll(battles.map { it.battle })
            db.allBattlesDynamoDataDao().insert(AllBattlesDynamoCount(all_battles_dynamo_count = 40))
            for (i in 1.. 40) {
                db.thumbnailSignedUrlDao().insertSignedUrl(ThumbnailSignedUrlCache(i,"ooo$i.jpg" ))
            }
            val allBattlesLoaded = repository.loadAllBattles(this)
            val pagedList = getValue(allBattlesLoaded.data)
            pagedList.loadAllData()
            assertThat(pagedList, `is`(notNullValue()))
            assertThat(pagedList.size, `is`(40))
            checkBattleListSame(pagedList, battles)
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun loadMoreFromNetwork() {
        runBlockingTest {
            val battles = getBattlesResponse(40)
            val battlesInDb = battles.subList(0, 15)
            val battlesGetFromNetwork1 = battles.subList(15, 20)
            val battlesGetFromNetwork2 = battles.subList(20, 25)
            val battlesGetFromNetwork3 = battles.subList(25, 30)
            val battlesGetFromNetwork4 = battles.subList(30, 35)
            val battlesGetFromNetwork5 = battles.subList(35, 40)
            `when`(battlesApi.getFriendsBattles((16..20).toList())).thenReturn(AsyncTaskResult(GetFriendsBattlesResponse().apply { sqlResult = battlesGetFromNetwork1.map { it.battle } }))
            `when`(battlesApi.getFriendsBattles((21..25).toList())).thenReturn(AsyncTaskResult(GetFriendsBattlesResponse().apply { sqlResult = battlesGetFromNetwork2.map { it.battle } }))
            `when`(battlesApi.getFriendsBattles((26..30).toList())).thenReturn(AsyncTaskResult(GetFriendsBattlesResponse().apply { sqlResult = battlesGetFromNetwork3.map { it.battle } }))
            `when`(battlesApi.getFriendsBattles((31..35).toList())).thenReturn(AsyncTaskResult(GetFriendsBattlesResponse().apply { sqlResult = battlesGetFromNetwork4.map { it.battle } }))
            `when`(battlesApi.getFriendsBattles((36..40).toList())).thenReturn(AsyncTaskResult(GetFriendsBattlesResponse().apply { sqlResult = battlesGetFromNetwork5.map { it.battle } }))

            `when`(allBattlesDynamoDbRepository.loadListFromDynamo(15, 19)).thenReturn((16..20).toList())
            `when`(allBattlesDynamoDbRepository.loadListFromDynamo(20, 24)).thenReturn((21..25).toList())
            `when`(allBattlesDynamoDbRepository.loadListFromDynamo(25, 29)).thenReturn((26..30).toList())
            `when`(allBattlesDynamoDbRepository.loadListFromDynamo(30, 34)).thenReturn((31..35).toList())
            `when`(allBattlesDynamoDbRepository.loadListFromDynamo(35, 39)).thenReturn((36..40).toList())
            `when`(allBattlesDynamoDbRepository.getBattlesCountDynamo()).thenReturn(40)

            db.battlesDao().insertAll(battlesInDb.map { it.battle })
            db.allBattlesDynamoDataDao().insert(AllBattlesDynamoCount(all_battles_dynamo_count = 40))
            for (i in 1..15) {
                db.thumbnailSignedUrlDao().insertSignedUrl(ThumbnailSignedUrlCache(i, "ooo$i.jpg"))
            }

            val allBattlesLoaded = repository.loadAllBattles(this)
            var pagedList = getValue(allBattlesLoaded.data)

            assertThat(pagedList, `is`(notNullValue()))
            assertThat(pagedList.size, `is`(15))
            pagedList.loadAllData()
            //set off AtItemAtEndLoaded boundary callback
            pagedList.loadAround(14)

            checkBattleListSame(pagedList, battlesInDb)
            verify(battlesApi, timeout(1000)).getFriendsBattles((16..20).toList())
            pagedList = getValue(allBattlesLoaded.data)
            assertThat(pagedList.size, `is`(20))
            pagedList.loadAllData()
            pagedList.loadAround(19)

            verify(battlesApi, timeout(1000)).getFriendsBattles((21..25).toList())
            pagedList = getValue(allBattlesLoaded.data)
            assertThat(pagedList.size, `is`(25))
            pagedList.loadAllData()
            pagedList.loadAround(24)

            verify(battlesApi, timeout(1000)).getFriendsBattles((26..30).toList())
            pagedList = getValue(allBattlesLoaded.data)
            assertThat(pagedList.size, `is`(30))
            pagedList.loadAllData()
            pagedList.loadAround(29)

            verify(battlesApi, timeout(1000)).getFriendsBattles((31..35).toList())
            pagedList = getValue(allBattlesLoaded.data)
            assertThat(pagedList.size, `is`(35))
            pagedList.loadAllData()
            pagedList.loadAround(34)

            verify(battlesApi, timeout(1000)).getFriendsBattles((36..40).toList())

            pagedList = getValue(allBattlesLoaded.data)
            assertThat(pagedList.size, `is`(40))
            pagedList.loadAllData()
            checkBattleListSame(pagedList, battles)
        }
    }



    @ExperimentalCoroutinesApi
    @Test
    fun newBattleFromNetwork() {
        runBlockingTest {
            val battles = getBattlesResponse(17)
            val battlesInDb = battles.subList(0, 15)
            val battlesGetFromNetwork1 = battles.subList(15, 17)

            `when`(battlesApi.getFriendsBattles(listOf(16, 17))).thenReturn(AsyncTaskResult(GetFriendsBattlesResponse().apply { sqlResult = battlesGetFromNetwork1.map { it.battle } }))
            `when`(battlesApi.getFriendsBattles((1..15).toList())).thenReturn(AsyncTaskResult(GetFriendsBattlesResponse().apply { sqlResult = battlesInDb.map { it.battle } }))
            `when`(allBattlesDynamoDbRepository.getBattlesCountDynamo()).thenReturn(42)
            `when`(allBattlesDynamoDbRepository.loadListFromDynamo(0, 1)).thenReturn(listOf(16,17))

            db.battlesDao().insertAll(battlesInDb.map { it.battle })
            db.allBattlesDynamoDataDao().insert(AllBattlesDynamoCount(all_battles_dynamo_count = 40))
            for (i in 1..15) {
                db.thumbnailSignedUrlDao().insertSignedUrl(ThumbnailSignedUrlCache(i, "ooo$i.jpg"))
            }

            val allBattlesLoaded = repository.loadAllBattles(this)
            var pagedList = getValue(allBattlesLoaded.data)
            checkBattleListSame(pagedList, battlesInDb)
            repository.updateBattles()
            verify(battlesApi, timeout(1000)).getFriendsBattles(listOf(16, 17))
            verify(battlesApi, timeout(1000)).getFriendsBattles((1..15).toList())
            //pagedList should be updated with 2 new battles that are added to the top
            pagedList = getValue(allBattlesLoaded.data)
            assertThat(pagedList.size, `is`(17))

        }
    }





    private fun checkBattleListSame(list1: List<AllBattlesBattle>, list2: List<AllBattlesBattle>){
        assertThat(list1.size, `is`(list2.size))
        for (i in list1.indices) {
            assertThat(list1.get(i).battle.battleId, `is`(list2[i].battle.battleId))
            assertThat(list1.get(i).battle.battleName, `is`(list2[i].battle.battleName))
            assertThat(list1.get(i).battle.challengerCognitoID, `is`(list2[i].battle.challengerCognitoID))
            assertThat(list1.get(i).battle.challengedCognitoID, `is`(list2[i].battle.challengedCognitoID))
            assertThat(list1.get(i).battle.challengerUsername, `is`(list2[i].battle.challengerUsername))
            assertThat(list1.get(i).battle.challengedUsername, `is`(list2[i].battle.challengedUsername))
            assertThat(list1.get(i).battle.voting, `is`(list2[i].battle.voting))
            assertThat(list1.get(i).battle.userHasVoted, `is`(list2[i].battle.userHasVoted))
            assertThat(list1.get(i).battle.videosUploaded, `is`(list2[i].battle.videosUploaded))
            assertThat(list1.get(i).battle.signedThumbnailUrl, `is`(list2[i].battle.signedThumbnailUrl))
        }
    }


    private fun getBattlesResponse(numBattles: Int) : List<AllBattlesBattle>{
        val battles = mutableListOf<AllBattlesBattle>()
        for (i in 1.. numBattles) {
            val rounds = (1..5).random()
            val b = Battle(i, "13123111$i", "13123111${i+1} ", "Sword$i", rounds)
                    .apply {
                        videosUploaded = rounds * 2
                        battleAccepted = true
                        userHasVoted = false
                        challengerUsername = "freddie"
                        challengedUsername = "sam"
                        voting = Voting(ChooseVotingFragment.VotingChoice.PUBLIC, ChooseVotingFragment.VotingLength.ONE_MONTH, Calendar.getInstance().time, (0..10).random(), (0..20).random())
                    }
                battles.add(AllBattlesBattle(b, "ooo$i.jpg"))
        }
        return battles
    }



    private fun <T> PagedList<T>.loadAllData() {
        do {
            val indexOfFirstNotLoadedItem = this.indexOfFirst {it == null }
            if (indexOfFirstNotLoadedItem != -1){
                this.loadAround(indexOfFirstNotLoadedItem)
            }
        } while (indexOfFirstNotLoadedItem != -1)
    }



    /**
     * Get the value from a LiveData object. We're waiting for LiveData to emit, for 2 seconds.
     * Once we got a notification via onChanged, we stop observing.
     */
    fun <T> getValue(liveData: LiveData<T>): T {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data[0] = o
                latch.countDown()
                liveData.removeObserver(this)
            }
        }
        liveData.observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)

        @Suppress("UNCHECKED_CAST")
        return data[0] as T
    }



}

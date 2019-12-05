package com.liamfarrell.android.snapbattle.data

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.liamfarrell.android.snapbattle.db.FollowingUserDao
import com.liamfarrell.android.snapbattle.db.FollowingUserDynamoCount
import com.liamfarrell.android.snapbattle.db.FollowingUserDynamoDataDao
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.schedulers.Schedulers.single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingUserCacheManager @Inject constructor(
         val followingDynamodbRepository: UserFollowingDynamodbRepository,
         val userDao: FollowingUserDao,
         val followingUserFollowingRepository: UserFollowingRepository,
         val followingUserDynamoDataDao: FollowingUserDynamoDataDao)
{




     @SuppressLint("CheckResult")
     fun loadFromScratch(){
        val usersFollowing = followingUserFollowingRepository.getFollowingRx()
        val usersFollowingDynamoDbCount = followingDynamodbRepository.getFollowingUpdateCountDynamo()
        usersFollowing.subscribeOn(io())
                 .observeOn(single())
                 .subscribe(
                         {successReponse ->
                             userDao.insertAll(successReponse.sqlResult)
                             followingUserDynamoDataDao.insert(FollowingUserDynamoCount(usersFollowingDynamoDbCount))
                         },
                         {onError-> throw onError}
                 )
    }

    fun checkForUpdates() : Completable {
        return Completable.fromCallable { performCheckForUpdates() }
    }

     private fun performCheckForUpdates() {
        val followingUpdateCountServer = followingDynamodbRepository.getFollowingUpdateCountDynamo()
        val followingUpdateCountCache = followingUserDynamoDataDao.getDynamoCount()

        //update the following user list of the following update count from server to cache is different
        if (followingUpdateCountServer != followingUpdateCountCache) {
            val actionList = followingDynamodbRepository.getActionListFromDynamo(0,followingUpdateCountServer - followingUpdateCountCache - 1 )
            //get the list of cognito ids of users to add to the cache
            val cognitoIDsToAdd = actionList.filter { it.m["ACTION"]?.s == ACTION_ADD }.mapNotNull { it.m["COGNITO_ID"]?.s }
            val usersResponse = followingUserFollowingRepository.getUsersRx(cognitoIDsToAdd)

            actionList.forEach { it ->
                val action = it.m["ACTION"]?.s
                val cognitoId = it.m["COGNITO_ID"]?.s
                if (action == ACTION_ADD) {
                    usersResponse.sqlResult.find { it.cognitoId == cognitoId}?.let {
                        userDao.insertAll(listOf(it))
                    }
                } else if (action == ACTION_REMOVE){
                     if (cognitoId != null) userDao.deleteFromUser(cognitoId)
                }
                else if (action == ACTION_UPDATE_NAME){
                    val name =  it.m["NAME"]?.s
                    if (cognitoId != null && name != null) userDao.updateName(cognitoId, name)
                }
                else if (action == ACTION_UPDATE_USERNAME){
                    val username = it.m["USERNAME"]?.s
                    if (cognitoId != null && username != null) userDao.updateUsername(cognitoId, username)

                }
            }

            //update the following update count
            followingUserDynamoDataDao.updateFollowingUserDynamoCount(followingUpdateCountServer)
        }
    }

     fun getUsersInCache() : List<User> {
        return userDao.getAllFollowingUsers()
    }


    fun searchUsersInCacheRx(searchQuery : String) : Single<List<User>> {
        return userDao.searchUsersInCacheRx(searchQuery)
    }



    companion object {
        private const val ACTION_ADD = "ADD"
        private const val ACTION_REMOVE = "REMOVE"
        private const val ACTION_UPDATE_USERNAME = "UPDATE_USERNAME"
        private const val ACTION_UPDATE_NAME = "UPDATE_NAME"
    }



}


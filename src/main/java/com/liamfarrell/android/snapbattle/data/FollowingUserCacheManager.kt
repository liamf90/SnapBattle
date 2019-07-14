package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import com.liamfarrell.android.snapbattle.db.FollowingUserDao
import com.liamfarrell.android.snapbattle.db.FollowingUserDynamoDataDao
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowingUserCacheManager @Inject constructor(
        private val followingDynamodbRepository: UserFollowingDynamodbRepository,
        private val userDao: FollowingUserDao,
        private val followingUserFollowingRepository: UserFollowingRepository,
        private val followingUserDynamoDataDao: FollowingUserDynamoDataDao)
{




    suspend fun checkForUpdates() : Unit {
        val followingUpdateCountServer = followingDynamodbRepository.getFollowingUpdateCountDynamo()
        val followingUpdateCountCache = followingUserDynamoDataDao.getDynamoCount()

        //update the following user list of the following update count from server to cache is different
        if (followingUpdateCountServer != followingUpdateCountCache) {
            val actionList = followingDynamodbRepository.getActionListFromDynamo(0,followingUpdateCountServer - followingUpdateCountCache - 1 )
            //get the list of cognito ids of users to add to the cache
            val cognitoIDsToAdd = actionList.filter { it.m["ACTION"]?.s == ACTION_ADD }.mapNotNull { it.m["COGNITO_ID"]?.s }
            val usersResponse = followingUserFollowingRepository.getUsers(cognitoIDsToAdd)
            if (usersResponse.error == null){
                actionList.forEach {
                    val action = it.m["ACTION"]?.s
                    val cognitoId = it.m["COGNITO_ID"]?.s
                    if (action == ACTION_ADD) {
                        usersResponse.result.sqlResult.find { it.cognitoId == cognitoId}?.let {
                            userDao.insertAll(listOf(it))
                        }
                    } else if (action == ACTION_REMOVE){
                         if (cognitoId != null) userDao.deleteFromUser(cognitoId)
                    }
                }
            }
            //update the following update count
            followingUserDynamoDataDao.updateFollowingUserDynamoCount(followingUpdateCountServer)
        }
    }

     fun getUsersInCache() : List<User> {
        return userDao.getAllFollowingUsers()
    }

    suspend fun searchUsersInCache(searchQuery : String) : List<User> {
        return userDao.searchUsersInCache(searchQuery)
    }



    companion object {
        private const val ACTION_ADD = "ADD"
        private const val ACTION_REMOVE = "REMOVE"
    }



}


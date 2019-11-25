package com.liamfarrell.android.snapbattle.data

import androidx.lifecycle.LiveData
import com.liamfarrell.android.snapbattle.db.FollowingUserDao
import com.liamfarrell.android.snapbattle.db.FollowingUserDynamoCount
import com.liamfarrell.android.snapbattle.db.FollowingUserDynamoDataDao
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult
import com.liamfarrell.android.snapbattle.model.User
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




    suspend fun loadFromScratch(){
        val usersFollowing = followingUserFollowingRepository.getFollowing()
        val usersFollowingDynamoDbCount = followingDynamodbRepository.getFollowingUpdateCountDynamo()
        if (usersFollowing.error != null){
            throw usersFollowing.error
        } else{
            withContext(Dispatchers.IO) {
                userDao.insertAll(usersFollowing.result.sqlResult)
                followingUserDynamoDataDao.insert(FollowingUserDynamoCount(usersFollowingDynamoDbCount))
            }
        }
    }

    suspend fun checkForUpdates() {
        val followingUpdateCountServer = followingDynamodbRepository.getFollowingUpdateCountDynamo()
        val followingUpdateCountCache = followingUserDynamoDataDao.getDynamoCount()

        //update the following user list of the following update count from server to cache is different
        if (followingUpdateCountServer != followingUpdateCountCache) {
            val actionList = followingDynamodbRepository.getActionListFromDynamo(0,followingUpdateCountServer - followingUpdateCountCache - 1 )
            //get the list of cognito ids of users to add to the cache
            val cognitoIDsToAdd = actionList.filter { it.m["ACTION"]?.s == ACTION_ADD }.mapNotNull { it.m["COGNITO_ID"]?.s }
            val usersResponse = followingUserFollowingRepository.getUsers(cognitoIDsToAdd)
            if (usersResponse.error == null){
                actionList.forEach { it ->
                    val action = it.m["ACTION"]?.s
                    val cognitoId = it.m["COGNITO_ID"]?.s
                    if (action == ACTION_ADD) {
                        usersResponse.result.sqlResult.find { it.cognitoId == cognitoId}?.let {
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
            }
            //update the following update count
            followingUserDynamoDataDao.updateFollowingUserDynamoCount(followingUpdateCountServer)
        }
    }

     suspend fun getUsersInCache() : List<User> {
        return userDao.getAllFollowingUsers()
    }

    suspend fun searchUsersInCache(searchQuery : String) : List<User> {
        return userDao.searchUsersInCache(searchQuery)
    }



    companion object {
        private const val ACTION_ADD = "ADD"
        private const val ACTION_REMOVE = "REMOVE"
        private const val ACTION_UPDATE_USERNAME = "UPDATE_USERNAME"
        private const val ACTION_UPDATE_NAME = "UPDATE_NAME"
    }



}


//package com.dicoding.picodiploma.loginwithanimation.data.local
//
//
//import androidx.paging.ExperimentalPagingApi
//import androidx.paging.LoadType
//import androidx.paging.PagingState
//import androidx.paging.RemoteMediator
//import androidx.room.withTransaction
//import com.dicoding.picodiploma.loginwithanimation.data.ListStoryItem
//import com.dicoding.picodiploma.loginwithanimation.data.api.ApiService
//
//@OptIn(ExperimentalPagingApi::class)
//class StoryRemoteMediator(
//    private val database: StoryDatabase,
//    private val apiService: ApiService,
//) : RemoteMediator<Int, ListStoryItem>() {
//
//    private companion object {
//        const val INITIAL_PAGE_INDEX = 1
//    }
//
//    override suspend fun initialize(): InitializeAction {
//        return InitializeAction.LAUNCH_INITIAL_REFRESH
//    }
//
//    override suspend fun load(
//        loadType: LoadType,
//        state: PagingState<Int, ListStoryItem>
//    ): MediatorResult {
//        val page = INITIAL_PAGE_INDEX
//
//        try {
//            val responseData = apiService.getStories(page, state.config.pageSize)
//            val endOfPaginationReached = responseData.isEmpty()
//            database.withTransaction {
//                if (loadType == LoadType.REFRESH) {
//                    database.storyDao().deleteAll()
//                }
//                database.storyDao().insertStory(responseData)
//            }
//            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
//        } catch (exception: Exception) {
//            return MediatorResult.Error(exception)
//        }
//    }
//}
package com.dicoding.picodiploma.loginwithanimation.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.picodiploma.loginwithanimation.DataDummy
import com.dicoding.picodiploma.loginwithanimation.MainDispatcherRule
import com.dicoding.picodiploma.loginwithanimation.data.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: UserRepository

    @Test
    fun `when Get Story Pager Should Not Null and Return Data`() = runBlocking {
        val dummyListStoryItem = DataDummy.generateDummyListStoryItem()
        val dummyPagingData = PagingData.from(dummyListStoryItem)
        val expectedValue = flowOf(dummyPagingData)
        Mockito.`when`(storyRepository.getStoryPager()).thenReturn(expectedValue)

        val mainViewModel = MainViewModel(storyRepository)
        val actualFlow = mainViewModel.getStoryPager()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        // Menggunakan actualFlow untuk submit data
        actualFlow.collectLatest { pagingData ->
            differ.submitData(pagingData)
        }

        // Verifikasi hasil
        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyListStoryItem.size, differ.snapshot().size)
        Assert.assertEquals(dummyListStoryItem[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Pager Empty Should Return No Data`() = runBlocking {
        val dummyListStoryItem: List<ListStoryItem> = emptyList()
        val dummyPagingData = PagingData.from(dummyListStoryItem)
        val expectedValue = flowOf(dummyPagingData)

        Mockito.`when`(storyRepository.getStoryPager()).thenReturn(expectedValue)

        val mainViewModel = MainViewModel(storyRepository)

        val actualFlow = mainViewModel.getStoryPager()

        val differ = AsyncPagingDataDiffer(
            diffCallback = MainAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        actualFlow.collectLatest { pagingData ->
            differ.submitData(pagingData)
        }

        Assert.assertEquals(expectedValue, actualFlow)
        Assert.assertEquals(0, differ.snapshot().size)

    }


    val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}


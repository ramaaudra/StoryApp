package com.dicoding.picodiploma.loginwithanimation.view.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.pager.LoadingStateAdapter
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.upload.UploadActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Setup actions and views
        setupAction()
        setupRecyclerView()

        // Observe session data from ViewModel to ensure the user is logged in
        lifecycleScope.launch {
            // Tunggu hingga token siap sebelum melakukan request API
            val user = viewModel.getSession().first()
            if (user.token.isNotEmpty()) {
                Log.d("MainActivity", "User token: ${user.token}")
                ApiConfig.updateApiService(UserPreference.getInstance(applicationContext.dataStore))
                viewModel.getStoryPager().collectLatest { pagingData ->
                    mainAdapter.submitData(pagingData)
                }
            } else {
                // Token belum siap, arahkan ke login
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when activity is resumed
        lifecycleScope.launch {
            viewModel.getStoryPager().collectLatest { pagingData ->
                mainAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupAction() {
        binding.fabAdd.setOnClickListener {
            // Navigate to UploadActivity when fab is clicked
            startActivity(Intent(this, UploadActivity::class.java))
        }

        binding.btnLogIut.setOnClickListener {
            // Logout the user and go back to login screen
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnMaps.setOnClickListener {
            // Navigate to MapsActivity
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        mainAdapter = MainAdapter()

        // Setup RecyclerView with LinearLayoutManager and adapter
        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.adapter = mainAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                mainAdapter.retry()
            }
        )

        // Listen for load states (loading, error, empty)
        mainAdapter.addLoadStateListener { loadState ->
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    showLoading(true)
                    showError(false)
                    showEmpty(false)
                }
                is LoadState.Error -> {
                    showLoading(false)
                    showError(true, "Terjadi kesalahan.")
                    showEmpty(false)
                }
                is LoadState.NotLoading -> {
                    showLoading(false)
                    if (loadState.append.endOfPaginationReached) {
                        showEmpty(true)
                    }
                    showError(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(isError: Boolean, message: String = "") {
        if (isError) {
            Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            Toast.makeText(this, "Tidak ada data tersedia", Toast.LENGTH_SHORT).show()
        }
    }
}

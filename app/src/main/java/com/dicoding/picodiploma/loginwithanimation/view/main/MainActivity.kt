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
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.pager.LoadingStateAdapter
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.upload.UploadActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.getSession().observe(this) { user ->
            if (user.token.isEmpty()) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                ApiConfig.updateApiService(user.token)
                viewModel.getStories()
            }
        }

        setupAction()
        setupRecyclerView()
        observeStories()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getStories()
    }

    private fun setupAction() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        binding.btnLogIut.setOnClickListener {
            viewModel.logout()
        }

        binding.btnMaps.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        mainAdapter = MainAdapter()
        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.adapter = mainAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    mainAdapter.retry()
                }
        )
    }

    private fun observeStories() {
        viewModel.getStoryPager().observe(this) { pagingData ->
            lifecycleScope.launch {
                mainAdapter.submitData(pagingData)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
package com.dicoding.picodiploma.loginwithanimation.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainAdapter: MainAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel.getSession().observe(this) { user ->
            Log.d("MainActivity", "Session observer triggered: $user")
            if (!user.isLogin || user.token.isEmpty()) {
                Log.d("MainActivity", "Invalid session, redirecting to login")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                viewModel.getStories()
            }
        }
        setupAction()
        setupRecyclerView()
        observeStories()
    }

    private fun setupAction() {
        binding.fabAdd.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun setupRecyclerView() {
        mainAdapter = MainAdapter(emptyList())
        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mainAdapter
        }
    }


    private fun observeStories() {
        viewModel.stories.observe(this) { resultState ->
            when (resultState) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    mainAdapter.updateData(resultState.data.listStory)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    // Check if token not found
                    if (resultState.message.contains("Token not found")) {
                        // Redirect ke login
                        viewModel.logout()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Gagal memuat story: ${resultState.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }



    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
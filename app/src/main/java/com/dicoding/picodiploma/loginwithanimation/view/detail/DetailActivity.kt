package com.dicoding.picodiploma.loginwithanimation.view.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.data.Story
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityDetailBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.main.MainViewModel

class DetailActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val storyId = intent.getStringExtra("STORY_ID") ?: return
        observeStory()
        viewModel.getStoryById(storyId)
    }

    private fun observeStory() {
        viewModel.story.observe(this, Observer { resultState ->
            when (resultState) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    displayStoryDetails(resultState.data.story)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.e("DetailActivity", "Error fetching story: ${resultState.message}")
                }
                is ResultState.Finished -> {
                    showLoading(false)
                }            }
        })
    }

    private fun displayStoryDetails(story: Story?) {
        story?.let {
            binding.tvItemNameDesc.text = it.name
            binding.tvItemDescriptionDesc.text = it.description
            Glide.with(this)
                .load(it.photoUrl)
                .into(binding.imgItemPhotoDesc)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
package com.dicoding.picodiploma.loginwithanimation.view.login

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
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityLoginBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.main.MainActivity
import com.dicoding.picodiploma.loginwithanimation.view.signup.SignupActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityLoginBinding
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Mengecek status login user pada saat activity dimulai
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        runBlocking {
            val user = viewModel.getSessionUser().firstOrNull()
            if (user != null && user.token.isNotEmpty()) {
                Log.d("LoginActivity", "User token: ${user.token}")
                // If the user is already logged in, navigate to MainActivity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()  // Stop LoginActivity
            } else {
                // If not logged in, set the content view to show the login screen
                binding = ActivityLoginBinding.inflate(layoutInflater)
                setContentView(binding.root)
                setupAction()
            }
        }
    }

    private fun setupAction() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginButton.setOnClickListener {
            if (validateFields()) {
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                showLoading(true)
                viewModel.login(email, password)
            }
        }

        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {}
                    is ResultState.Success -> {
                        showLoading(false)
                        delay(500)
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    is ResultState.Error -> {
                        showLoading(false)
                        Toast.makeText(
                            this@LoginActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    ResultState.Finished -> showLoading(false)
                }
            }
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateFields(): Boolean {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        return when {
            email.isEmpty() -> {
                showToast(getString(R.string.empty_email_warning))
                false
            }
            password.isEmpty() -> {
                showToast(getString(R.string.empty_password_warning))
                false
            }
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
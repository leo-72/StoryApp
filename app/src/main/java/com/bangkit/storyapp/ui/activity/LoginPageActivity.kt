package com.bangkit.storyapp.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bangkit.storyapp.MainActivity
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.model.UserModel
import com.bangkit.storyapp.data.model.UserModelPreferences
import com.bangkit.storyapp.data.remote.response.ResultResponse
import com.bangkit.storyapp.databinding.ActivityLoginPageBinding
import com.bangkit.storyapp.helper.Helper
import com.bangkit.storyapp.ui.view_model.LoginViewModel
import com.bangkit.storyapp.ui.view_model.UserViewModel
import com.bangkit.storyapp.ui.view_model.ViewModelFactory
import com.bangkit.storyapp.ui.view_model.ViewModelUserFactory
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_pref")

class LoginPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPageBinding
    private val loginViewModel: LoginViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setMyButtonEnable()
        editTextListener()
        buttonListener()
    }

    private fun editTextListener() {
        binding.inpEditEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        binding.inpEditPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMyButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterPageActivity::class.java))
            finish()
        }
    }

    private fun setMyButtonEnable() {
        val resultEmail = binding.inpEditEmail.text
        val resultPass = binding.inpEditPass.text

        binding.btnSignIn.isEnabled = resultPass != null && resultEmail != null &&
                binding.inpEditPass.text.toString().length >= 6 &&
                Helper.isEmailValid(binding.inpEditEmail.text.toString())
    }

    private fun showToast(param: Boolean, message: String) {
        if (param) {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(applicationContext, UserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

        } else {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
            binding.progressBarLogin.visibility = View.GONE
        }
    }

    private fun buttonListener() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.inpEditEmail.text.toString()
            val pass = binding.inpEditPass.text.toString()

            loginViewModel.login(email, pass).observe(this) {
                when (it) {
                    is ResultResponse.Loading -> {
                        binding.progressBarLogin.visibility = View.VISIBLE
                    }
                    is ResultResponse.Success -> {
                        binding.progressBarLogin.visibility = View.GONE
                        val user = UserModel(
                            it.data.name,
                            email,
                            pass,
                            it.data.userId,
                            it.data.token,
                            true
                        )
                        showToast(true, getString(R.string.success_login))

                        val userPref = UserModelPreferences.getInstance(dataStore)
                        lifecycleScope.launchWhenStarted {
                            userPref.saveUser(user)
                        }
                    }
                    is ResultResponse.Error -> {
                        binding.progressBarLogin.visibility = View.GONE
                        showToast(false, it.error)
                    }
                }
            }
        }

        binding.btnLang.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
    }
}
package com.bangkit.storyapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.remote.response.ResultResponse
import com.bangkit.storyapp.databinding.ActivityRegisterPageBinding
import com.bangkit.storyapp.helper.Helper
import com.bangkit.storyapp.ui.view_model.RegisterViewModel
import com.bangkit.storyapp.ui.view_model.ViewModelFactory

class RegisterPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPageBinding
    private val registerViewModel : RegisterViewModel by viewModels{
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterPageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        btnEnable()
        editTextListener()
        btnActivity()
    }

    private fun editTextListener() {
        binding.inpEditName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        binding.inpEditEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        binding.inpEditPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        binding.inpEditRepass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnEnable()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        binding.btnSignIn.setOnClickListener {
            startActivity(Intent(this, LoginPageActivity::class.java))
            finish()
        }
    }

    private fun btnEnable() {
        binding.btnSignUp.isEnabled =
            binding.inpEditName.text.toString().isNotEmpty() &&
            binding.inpEditEmail.text.toString().isNotEmpty() &&
                    binding.inpEditPass.text.toString().isNotEmpty() &&
                    binding.inpEditRepass.text.toString().isNotEmpty() &&
                    binding.inpEditPass.text.toString().length >= 6 &&
                    Helper.isEmailValid(binding.inpEditEmail.text.toString())
    }

    private fun btnActivity() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.inpEditName.text.toString()
            val email = binding.inpEditEmail.text.toString()
            val password = binding.inpEditPass.text.toString()

            registerViewModel.register(name, email, password).observe(this){
                when (it) {
                    is ResultResponse.Loading -> {
                        binding.progressBarRegister.visibility = View.VISIBLE
                    }
                    is ResultResponse.Success -> {
                        binding.progressBarRegister.visibility = View.GONE
                        showToast(true, getString(R.string.register_success))
                    }
                    is ResultResponse.Error -> {
                        binding.progressBarRegister.visibility = View.GONE
                        showToast(false, it.error)
                    }
                }
            }
        }
        binding.btnLang.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }
    }

    private fun showToast(params: Boolean, message: String) {
        binding.apply {
            val pass = inpEditPass.text.toString()
            val repass = inpEditRepass.text.toString()

            if (pass.equals(repass)) {
                if (params) {
                    Toast.makeText(applicationContext, getString(R.string.register_success), Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(applicationContext, LoginPageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, getString(R.string.register_failed), Toast.LENGTH_SHORT).show()
                    progressBarRegister.visibility = View.GONE
                    val email = inpEditEmail
                    email.text = Editable.Factory.getInstance().newEditable("")
                }
            } else {
                Toast.makeText(applicationContext, "Passwords are not the same", Toast.LENGTH_SHORT)
                    .show()
                progressBarRegister.visibility = View.GONE
            }
        }
    }
}
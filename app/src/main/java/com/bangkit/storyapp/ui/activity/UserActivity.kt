package com.bangkit.storyapp.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.bangkit.storyapp.databinding.ActivityUserBinding
import com.bangkit.storyapp.ui.view_model.UserViewModel
import com.bangkit.storyapp.ui.view_model.ViewModelUserFactory
import kotlinx.coroutines.launch


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_pref")

class UserActivity : AppCompatActivity() {
    private lateinit var userModel: UserModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        buttonListener()
    }

    private fun setupViewModel() {
        userViewModel = ViewModelProvider(
            this,
            ViewModelUserFactory(UserModelPreferences.getInstance(dataStore))
        )[UserViewModel::class.java]

        lifecycleScope.launchWhenCreated {
            launch {
                userViewModel.getUser().collect {
                    userModel = UserModel(
                        it.name,
                        it.email,
                        it.password,
                        it.userId,
                        it.token,
                        true
                    )
                    binding.txtHi.text = getString(R.string.txt_hi, userModel.name)
                }
            }
        }
    }

    private fun buttonListener() {
        binding.btnContinue.setOnClickListener {
            val moveToListStoryActivity = Intent(this, MainActivity::class.java)
            moveToListStoryActivity.putExtra(MainActivity.EXTRA_USER, userModel)
            startActivity(moveToListStoryActivity)
        }
    }
}
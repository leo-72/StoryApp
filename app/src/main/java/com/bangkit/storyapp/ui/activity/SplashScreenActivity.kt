package com.bangkit.storyapp.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bangkit.storyapp.MainActivity
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.model.UserModel
import com.bangkit.storyapp.data.model.UserModelPreferences
import com.bangkit.storyapp.databinding.ActivitySplashScreenBinding
import com.bangkit.storyapp.ui.view_model.UserViewModel
import com.bangkit.storyapp.ui.view_model.ViewModelUserFactory
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_pref")

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var userModel: UserModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var options: ActivityOptionsCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topAnim = AnimationUtils.loadAnimation(this, R.anim.top_anim)
        val botAnim = AnimationUtils.loadAnimation(this, R.anim.bot_anim)
        binding.logoStoryApp.startAnimation(topAnim)
        binding.txtStoryApp.startAnimation(botAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            setupViewModel()
            finish()
        }, 2000)
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
                    if (it.isLogin) gotoMainActivity(true)
                    else gotoMainActivity(false)
                }
            }
        }
    }

    private fun gotoMainActivity(boolean: Boolean) {
        if (boolean) {
            val moveToListStoryActivity = Intent(this, UserActivity::class.java)
            moveToListStoryActivity.putExtra(MainActivity.EXTRA_USER, userModel)
            startActivity(
                moveToListStoryActivity,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
            )
            finish()
        } else {
            // Shared Element
            options =
                ViewCompat.getTransitionName(binding.logoStoryApp)?.let { it1 ->
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        binding.logoStoryApp, it1
                    )
                }!!
            options =
                ViewCompat.getTransitionName(binding.txtStoryApp)?.let { it1 ->
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        binding.txtStoryApp, it1
                    )
                }!!
            startActivity(
                Intent(this, LoginPageActivity::class.java),
                options.toBundle()
            )
            finish()
        }
    }
}
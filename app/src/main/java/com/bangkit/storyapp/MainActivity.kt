package com.bangkit.storyapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.storyapp.data.model.UserModel
import com.bangkit.storyapp.data.model.UserModelPreferences
import com.bangkit.storyapp.databinding.ActivityMainBinding
import com.bangkit.storyapp.ui.activity.LoginPageActivity
import com.bangkit.storyapp.ui.activity.MapsActivity
import com.bangkit.storyapp.ui.activity.NewStoryActivity
import com.bangkit.storyapp.ui.adapter.LoadingStateAdapter
import com.bangkit.storyapp.ui.adapter.StoryAdapter
import com.bangkit.storyapp.ui.view_model.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_pref")

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding

    private lateinit var userModel: UserModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var adapter: StoryAdapter

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        userModel = intent.getParcelableExtra(EXTRA_USER)!!

        initSwipeToRefresh()
        setupViewModel()
        initAdapter()
        initToolbar()
        addStory()
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
                }
            }
        }
    }

    private fun initAdapter() {
        adapter = StoryAdapter()
        binding?.rvListStory?.adapter = adapter.withLoadStateHeaderAndFooter(
            footer = LoadingStateAdapter { adapter.retry()},
            header = LoadingStateAdapter { adapter.retry() }
        )
        binding?.rvListStory?.layoutManager = LinearLayoutManager(this)
        binding?.rvListStory?.setHasFixedSize(true)

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collect {
                binding?.refreshLayout?.isRefreshing = it.mediator?.refresh is LoadState.Loading
            }
        }
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                binding?.showError?.root?.isVisible = loadStates.refresh is LoadState.Error
            }
            if (adapter.itemCount < 1) binding?.showError?.root?.visibility = View.VISIBLE
            else binding?.showError?.root?.visibility = View.GONE
        }

        viewModel.getStory(userModel.token).observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    // update data when swipe
    @SuppressLint("NotifyDataSetChanged")
    private fun initSwipeToRefresh() {
        binding?.refreshLayout?.setOnRefreshListener {
            adapter.refresh()
            adapter.notifyDataSetChanged()}
    }

    private fun initToolbar() {
        setSupportActionBar(binding?.toolbarHome)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun addStory() {
        binding?.addStoryFloat?.setOnClickListener {
            val moveToAddStoryActivity = Intent(this, NewStoryActivity::class.java)
            moveToAddStoryActivity.putExtra(NewStoryActivity.EXTRA_USER, userModel)
            startActivity(moveToAddStoryActivity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.map -> {
                val showMapStory = Intent(this, MapsActivity::class.java)
                showMapStory.putExtra(NewStoryActivity.EXTRA_USER, userModel)
                startActivity(showMapStory)
                true
            }
            R.id.language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.logout -> {
                AlertDialog.Builder(this).apply {
                    setTitle(getString(R.string.str_logout))
                    setMessage(getString(R.string.str_ask_to_logout))
                    setPositiveButton(getString(R.string.str_yes)) { _, _ ->
                        val intent = Intent(context, LoginPageActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        userViewModel.logout()
                        finish()
                    }
                    setNegativeButton(getString(R.string.str_no)) { _, _ ->
                        setCancelable(true)
                    }
                    create()
                    show()
                }
                true
            }
            else -> false
        }
    }

    companion object {
        const val EXTRA_USER = "user"
    }
}
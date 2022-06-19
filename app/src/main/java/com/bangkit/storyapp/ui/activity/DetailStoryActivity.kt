package com.bangkit.storyapp.ui.activity

import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.remote.response.ListStoryItem
import com.bangkit.storyapp.databinding.ActivityDetailStoryBinding
import com.bangkit.storyapp.helper.Helper
import com.bangkit.storyapp.ui.view_model.DetailStoryViewModel
import com.bumptech.glide.Glide
import java.util.*

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var listStory: ListStoryItem
    private lateinit var binding: ActivityDetailStoryBinding

    private val detailStoryViewModel: DetailStoryViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.descDetailStory.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }

        listStory = intent.getParcelableExtra(EXTRA_STORY)!!
        detailStoryViewModel.setDetailStory(listStory)
        displayResult()
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarDetailStory)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayResult() {
        with(binding) {
            nameDetailStory.text = detailStoryViewModel.storyItem.name
            uploadedDetailStory.text = getString(R.string.created_at,Helper.formatDate(detailStoryViewModel.storyItem.createdAt, TimeZone.getDefault().id))
            descDetailStory.text = detailStoryViewModel.storyItem.desc

            Glide.with(imgViewDetailStory)
                .load(detailStoryViewModel.storyItem.photoUrl)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_no_image)
                .into(imgViewDetailStory)
        }
    }

    companion object {
        const val EXTRA_STORY = "story"
    }
}
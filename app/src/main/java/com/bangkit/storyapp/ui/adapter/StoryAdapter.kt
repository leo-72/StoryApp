package com.bangkit.storyapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.remote.response.ListStoryItem
import com.bangkit.storyapp.databinding.ItemListStoryBinding
import com.bangkit.storyapp.helper.Helper
import com.bangkit.storyapp.ui.activity.DetailStoryActivity
import com.bangkit.storyapp.ui.activity.LoginPageActivity
import com.bumptech.glide.Glide
import java.util.*

class StoryAdapter : PagingDataAdapter<ListStoryItem, StoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var options: ActivityOptionsCompat

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    inner class ViewHolder(private var binding: ItemListStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: ListStoryItem) {
            with(binding) {
                Glide.with(imgViewListStory)
                    .load(story.photoUrl) // URL Avatar
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_no_image)
                    .into(imgViewListStory)
                nameListStory.text = story.name
                uploadedListStory.text =
                    binding.root.resources.getString(
                        R.string.created_at,
                        Helper.formatDate(story.createdAt, TimeZone.getDefault().id)
                    )

                // image OnClickListener
                imgViewListStory.setOnClickListener {
                    options =
                        ViewCompat.getTransitionName(imgViewListStory)?.let { it1 ->
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                it.context as Activity,
                                imgViewListStory, it1
                            )
                        }!!
                    options =
                        ViewCompat.getTransitionName(nameListStory)?.let { it1 ->
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                it.context as Activity,
                                nameListStory, it1
                            )
                        }!!
                    options =
                        ViewCompat.getTransitionName(uploadedListStory)?.let { it1 ->
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                it.context as Activity,
                                uploadedListStory, it1
                            )
                        }!!
                    val intent = Intent(it.context, DetailStoryActivity::class.java)
                    intent.putExtra(DetailStoryActivity.EXTRA_STORY, story)
                    it.context.startActivity(intent, options.toBundle())
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}
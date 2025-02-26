package com.example.rentease.ui.propertydetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.rentease.databinding.ActivityFullScreenImageBinding

/**
 * Activity for displaying property images in full-screen mode
 */
class FullScreenImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullScreenImageBinding
    private lateinit var adapter: FullScreenImageAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupImageGallery()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Property Gallery"
    }
    
    private fun setupImageGallery() {
        val imageUrls = intent.getStringArrayListExtra(EXTRA_IMAGES) ?: arrayListOf()
        val currentPosition = intent.getIntExtra(EXTRA_POSITION, 0)
        
        adapter = FullScreenImageAdapter(imageUrls)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(currentPosition, false)
        
        // Update the counter when swiping through images
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.counter.text = getString(
                    com.example.rentease.R.string.image_counter, 
                    position + 1, 
                    imageUrls.size
                )
            }
        })
        
        // Set initial counter text
        binding.counter.text = getString(
            com.example.rentease.R.string.image_counter, 
            currentPosition + 1, 
            imageUrls.size
        )
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Close activity when back button in toolbar is clicked
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    companion object {
        private const val EXTRA_IMAGES = "extra_images"
        private const val EXTRA_POSITION = "extra_position"
        
        fun createIntent(context: Context, imageUrls: ArrayList<String>, position: Int): Intent {
            return Intent(context, FullScreenImageActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_IMAGES, imageUrls)
                putExtra(EXTRA_POSITION, position)
            }
        }
    }
}

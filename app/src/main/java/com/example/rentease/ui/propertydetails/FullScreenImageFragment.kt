package com.example.rentease.ui.propertydetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.rentease.R
import com.example.rentease.databinding.FragmentFullScreenImageBinding
import com.example.rentease.ui.base.BaseFragment

/**
 * Fragment for displaying property images in full-screen mode.
 * It replaces the FullScreenImageActivity in the fragment-based architecture.
 */
class FullScreenImageFragment : BaseFragment<FragmentFullScreenImageBinding>() {
    
    private val args: FullScreenImageFragmentArgs by navArgs()
    private lateinit var adapter: FullScreenImageAdapter
    
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFullScreenImageBinding {
        return FragmentFullScreenImageBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        setupToolbar()
        setupImageGallery()
    }
    
    private fun setupToolbar() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appCompatActivity.supportActionBar?.title = "Property Gallery"
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupImageGallery() {
        // Get the array of image URLs from arguments
        val imageUrls = args.imageUrls.toList()
        val currentPosition = args.position.coerceIn(0, maxOf(0, imageUrls.size - 1))
        
        adapter = FullScreenImageAdapter(imageUrls)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(currentPosition, false)
        
        // Update the counter when swiping through images
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.counter.text = getString(
                    R.string.image_counter,
                    position + 1,
                    imageUrls.size
                )
            }
        })
        
        // Set initial counter text
        binding.counter.text = getString(
            R.string.image_counter,
            currentPosition + 1,
            imageUrls.size
        )
    }
    
    override fun setupObservers() {
        // No observers needed for this fragment
    }
    
    companion object {
        fun newInstance() = FullScreenImageFragment()
    }
}

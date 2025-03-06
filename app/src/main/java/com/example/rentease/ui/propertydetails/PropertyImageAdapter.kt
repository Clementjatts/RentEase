package com.example.rentease.ui.propertydetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.rentease.R
import com.example.rentease.databinding.ItemPropertyImageBinding

/**
 * Adapter for displaying property images in a ViewPager
 */
class PropertyImageAdapter(
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<PropertyImageAdapter.ImageViewHolder>() {
    
    private var imageUrls: List<String> = emptyList()
    
    fun submitList(list: List<String>) {
        imageUrls = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemPropertyImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size

    inner class ImageViewHolder(
        private val binding: ItemPropertyImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onImageClick(imageUrls[position])
                }
            }
        }

        fun bind(imageUrl: String) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.placeholder_image)
                .into(binding.propertyImage)
        }
    }
}

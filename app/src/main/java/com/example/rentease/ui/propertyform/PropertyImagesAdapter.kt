package com.example.rentease.ui.propertyform

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentease.databinding.ItemPropertyImageBinding

/**
 * Adapter for displaying property images in a RecyclerView.
 */
class PropertyImagesAdapter(
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<Uri, PropertyImagesAdapter.ImageViewHolder>(ImageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemPropertyImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
    
    inner class ImageViewHolder(
        private val binding: ItemPropertyImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(uri: Uri, position: Int) {
            binding.propertyImage.setImageURI(uri)
            
            binding.deleteButton.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }
    
    private class ImageDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
        
        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
}

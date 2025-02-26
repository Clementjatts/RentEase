package com.example.rentease.ui.propertyform

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentease.databinding.ItemPropertyImageBinding
import com.example.rentease.utils.ImageLoader

class PropertyImagesAdapter(
    private val onRemoveClick: (Uri) -> Unit
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
        holder.bind(getItem(position))
    }

    inner class ImageViewHolder(
        private val binding: ItemPropertyImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val imageLoader = ImageLoader.getInstance(binding.root.context)

        fun bind(uri: Uri) {
            if (uri.scheme == "http" || uri.scheme == "https") {
                imageLoader.loadImage(binding.propertyImage, uri.toString())
            } else {
                binding.propertyImage.setImageURI(uri)
            }

            binding.deleteButton.setOnClickListener {
                onRemoveClick(uri)
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

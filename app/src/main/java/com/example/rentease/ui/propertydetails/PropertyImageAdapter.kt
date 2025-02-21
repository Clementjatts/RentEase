package com.example.rentease.ui.propertydetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentease.databinding.ItemPropertyImageBinding
import com.example.rentease.data.model.PropertyImage

class PropertyImageAdapter(
    private val onImageClick: (PropertyImage) -> Unit
) : ListAdapter<PropertyImage, PropertyImageAdapter.ImageViewHolder>(ImageDiffCallback()) {

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

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onImageClick(getItem(position))
                }
            }
        }

        fun bind(image: PropertyImage) {
            Glide.with(binding.root)
                .load(image.url)
                .centerCrop()
                .into(binding.propertyImage)
        }
    }

    private class ImageDiffCallback : DiffUtil.ItemCallback<PropertyImage>() {
        override fun areItemsTheSame(oldItem: PropertyImage, newItem: PropertyImage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PropertyImage, newItem: PropertyImage): Boolean {
            return oldItem == newItem
        }
    }
}

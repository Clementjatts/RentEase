package com.example.rentease.ui.landlord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentease.R
import com.example.rentease.data.model.Property
import com.example.rentease.databinding.ItemPropertyManagementBinding

/**
 * RecyclerView adapter for displaying properties in the property management screen.
 * Supports click events for editing and deleting properties.
 */
class PropertyManagementAdapter(
    private val onItemClick: (Property) -> Unit,
    private val onDeleteClick: (Property) -> Unit
) : ListAdapter<Property, PropertyManagementAdapter.PropertyViewHolder>(PropertyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyManagementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PropertyViewHolder(
        private val binding: ItemPropertyManagementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(property: Property) {
            binding.apply {
                // Set property details
                propertyTitle.text = property.title
                propertyPrice.text = root.context.getString(R.string.price_format, property.price)
                propertyAddress.text = property.address.ifEmpty { 
                    root.context.getString(R.string.no_address_provided) 
                }
                
                // Set property specifications
                val specs = buildString {
                    append("${property.bedroomCount} bed")
                    if (property.bathroomCount > 0) {
                        append(" • ${property.bathroomCount} bath")
                    }
                    if (property.furnitureType.isNotEmpty()) {
                        append(" • ${property.furnitureType.replaceFirstChar { it.uppercase() }}")
                    }
                }
                propertySpecs.text = specs

                // Load property image
                if (!property.imageUrl.isNullOrEmpty()) {
                    propertyImage.visibility = View.VISIBLE
                    Glide.with(root.context)
                        .load(property.imageUrl)
                        .placeholder(R.drawable.placeholder_property)
                        .error(R.drawable.placeholder_property)
                        .centerCrop()
                        .into(propertyImage)
                } else {
                    propertyImage.visibility = View.VISIBLE
                    propertyImage.setImageResource(R.drawable.placeholder_property)
                }

                // Set click listeners
                root.setOnClickListener { onItemClick(property) }
                deleteButton.setOnClickListener { onDeleteClick(property) }
                editButton.setOnClickListener { onItemClick(property) }
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class PropertyDiffCallback : DiffUtil.ItemCallback<Property>() {
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }
    }
}

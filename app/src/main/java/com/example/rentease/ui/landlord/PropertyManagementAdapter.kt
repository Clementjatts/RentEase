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
import com.example.rentease.databinding.ItemPropertyBinding

// RecyclerView adapter for displaying properties in the property management screen
class PropertyManagementAdapter(
    private val onItemClick: (Property) -> Unit,
    private val onDeleteClick: (Property) -> Unit
) : ListAdapter<Property, PropertyManagementAdapter.PropertyViewHolder>(PropertyDiffCallback()) {

    // Creates new ViewHolder instances for property items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }

    // Binds property data to ViewHolder at specified position
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PropertyViewHolder(
        private val binding: ItemPropertyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Binds property data to the view elements
        fun bind(property: Property) {
            binding.apply {
                // Set property details
                propertyTitle.text = property.title
                propertyPrice.text = root.context.getString(R.string.price_format, String.format("%.0f", property.price))

                // Set location with address first, then bed/bath info
                val locationText = buildString {
                    // Add address first
                    if (property.address.isNotEmpty()) {
                        append(property.address)
                    } else {
                        append(root.context.getString(R.string.no_address_provided))
                    }
                    // Add bed/bath specifications after address
                    append(" • ${property.bedroomCount} bed")
                    if (property.bathroomCount > 0) {
                        append(" • ${property.bathroomCount} bath")
                    }
                }
                propertyLocation.text = locationText

                // Set property description (furniture type already shown in chip, so just description)
                propertyDescription.text = if (!property.description.isNullOrEmpty()) {
                    property.description
                } else {
                    root.context.getString(R.string.no_description_provided)
                }

                // Set property type chip to show furniture type (since it's more relevant for users)
                if (!property.furnitureType.isNullOrEmpty() && property.furnitureType != "unfurnished") {
                    propertyType.text = property.furnitureType.replaceFirstChar { it.uppercase() }
                    propertyType.visibility = View.VISIBLE
                } else if (!property.type.isNullOrEmpty()) {
                    propertyType.text = property.type.replaceFirstChar { it.uppercase() }
                    propertyType.visibility = View.VISIBLE
                } else {
                    propertyType.visibility = View.GONE
                }

                // Set landlord name
                landlordName.text = if (!property.landlordName.isNullOrEmpty()) {
                    root.context.getString(R.string.listed_by_format, property.landlordName)
                } else {
                    root.context.getString(R.string.landlord_info_unavailable)
                }

                // Load property image
                propertyImage.visibility = View.VISIBLE
                if (!property.imageUrl.isNullOrEmpty()) {
                    Glide.with(root.context)
                        .load(property.imageUrl)
                        .placeholder(R.drawable.placeholder_property)
                        .error(R.drawable.placeholder_property)
                        .centerCrop()
                        .into(propertyImage)
                } else {
                    propertyImage.setImageResource(R.drawable.placeholder_property)
                }

                // Show delete button for management screen
                deleteButton.visibility = View.VISIBLE

                // Set click listeners
                root.setOnClickListener { onItemClick(property) }
                deleteButton.setOnClickListener { onDeleteClick(property) }
            }
        }
    }

    // DiffUtil callback for efficient list updates
    private class PropertyDiffCallback : DiffUtil.ItemCallback<Property>() {
        // Checks if two property items represent the same entity
        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.id == newItem.id
        }

        // Checks if two property items have the same content
        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }
    }
}

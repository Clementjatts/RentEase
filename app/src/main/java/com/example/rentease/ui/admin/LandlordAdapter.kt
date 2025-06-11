package com.example.rentease.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentease.data.model.User
import com.example.rentease.databinding.ItemLandlordBinding

// RecyclerView adapter for displaying landlords in the landlord management screen
class LandlordAdapter(
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, LandlordAdapter.LandlordViewHolder>(LandlordDiffCallback()) {

    // Creates and returns a new ViewHolder for landlord items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandlordViewHolder {
        val binding = ItemLandlordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LandlordViewHolder(binding)
    }

    // Binds landlord data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: LandlordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder class that holds and manages landlord item views
    inner class LandlordViewHolder(
        private val binding: ItemLandlordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Binds landlord data to the view elements
        fun bind(user: User) {
            binding.apply {
                nameTextView.text = user.fullName ?: user.username // Use consistent field name
                emailTextView.text = user.email

                // Set up edit and delete buttons
                editButton.setOnClickListener {
                    onEditClick(user)
                }

                deleteButton.setOnClickListener {
                    onDeleteClick(user)
                }
            }
        }
    }

    // DiffUtil callback for efficient list updates
    private class LandlordDiffCallback : DiffUtil.ItemCallback<User>() {
        // Checks if two items represent the same landlord
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        // Checks if the contents of two landlords are the same
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}

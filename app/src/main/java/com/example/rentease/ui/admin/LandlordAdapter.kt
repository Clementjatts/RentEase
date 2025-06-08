package com.example.rentease.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentease.data.model.User
import com.example.rentease.databinding.ItemLandlordBinding

/**
 * LandlordAdapter is a RecyclerView adapter for displaying landlords in the landlord management screen.
 * Updated to use User model for consistency.
 */
class LandlordAdapter(
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, LandlordAdapter.LandlordViewHolder>(LandlordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandlordViewHolder {
        val binding = ItemLandlordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LandlordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LandlordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LandlordViewHolder(
        private val binding: ItemLandlordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

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

    private class LandlordDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}

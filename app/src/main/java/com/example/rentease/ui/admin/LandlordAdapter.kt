package com.example.rentease.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentease.data.model.Landlord
import com.example.rentease.databinding.ItemLandlordBinding

/**
 * LandlordAdapter is a RecyclerView adapter for displaying landlords in the landlord management screen.
 */
class LandlordAdapter(
    private val onEditClick: (Landlord) -> Unit,
    private val onDeleteClick: (Landlord) -> Unit
) : ListAdapter<Landlord, LandlordAdapter.LandlordViewHolder>(LandlordDiffCallback()) {

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

        fun bind(landlord: Landlord) {
            binding.apply {
                nameTextView.text = landlord.name
                emailTextView.text = landlord.email

                // Set up edit and delete buttons
                editButton.setOnClickListener {
                    onEditClick(landlord)
                }

                deleteButton.setOnClickListener {
                    onDeleteClick(landlord)
                }
            }
        }
    }

    private class LandlordDiffCallback : DiffUtil.ItemCallback<Landlord>() {
        override fun areItemsTheSame(oldItem: Landlord, newItem: Landlord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Landlord, newItem: Landlord): Boolean {
            return oldItem == newItem
        }
    }
}

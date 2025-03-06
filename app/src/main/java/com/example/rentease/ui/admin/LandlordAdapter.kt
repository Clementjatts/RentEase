package com.example.rentease.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentease.data.model.Landlord
import com.example.rentease.databinding.ItemLandlordBinding
import java.util.Locale

/**
 * LandlordAdapter is a RecyclerView adapter for displaying landlords in the user management screen.
 */
class LandlordAdapter(
    private val onApproveClick: (Landlord) -> Unit,
    private val onRejectClick: (Landlord) -> Unit
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
                
                // Show approve/reject buttons only for pending landlords
                if (landlord.status == "pending") {
                    approveButton.visibility = android.view.View.VISIBLE
                    rejectButton.visibility = android.view.View.VISIBLE
                    statusTextView.text = "Status: Pending Approval"
                } else {
                    approveButton.visibility = android.view.View.GONE
                    rejectButton.visibility = android.view.View.GONE
                    statusTextView.text = "Status: ${landlord.status.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }}"
                }
                
                approveButton.setOnClickListener {
                    onApproveClick(landlord)
                }
                
                rejectButton.setOnClickListener {
                    onRejectClick(landlord)
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

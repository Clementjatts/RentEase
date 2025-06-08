package com.example.rentease.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentease.data.model.Request
import com.example.rentease.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying notifications in RecyclerView
 */
class NotificationAdapter(
    private val onItemClick: (Request) -> Unit
) : ListAdapter<Request, NotificationAdapter.NotificationViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: Request) {
            binding.apply {
                // Set requester name
                requesterName.text = request.requesterName

                // Set property title
                propertyTitle.text = request.propertyTitle

                // Set contact info
                val contactInfo = buildString {
                    append(request.requesterEmail)
                    if (!request.requesterPhone.isNullOrBlank()) {
                        append(" • ${request.requesterPhone}")
                    }
                }
                this.contactInfo.text = contactInfo

                // Set message preview
                messagePreview.text = request.message

                // Set timestamp
                timestamp.text = formatTimestamp(request.createdAt)

                // Show/hide unread indicator
                unreadIndicator.visibility = if (request.isRead) View.GONE else View.VISIBLE

                // Set click listener
                root.setOnClickListener {
                    onItemClick(request)
                }
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                // Parse the timestamp from backend (assuming ISO format)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(timestamp)
                
                if (date != null) {
                    val now = Date()
                    val diffInMillis = now.time - date.time
                    val diffInHours = diffInMillis / (1000 * 60 * 60)
                    val diffInDays = diffInHours / 24

                    when {
                        diffInHours < 1 -> "Just now"
                        diffInHours < 24 -> "${diffInHours}h ago"
                        diffInDays < 7 -> "${diffInDays}d ago"
                        else -> {
                            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                            outputFormat.format(date)
                        }
                    }
                } else {
                    "Unknown"
                }
            } catch (e: Exception) {
                "Unknown"
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class RequestDiffCallback : DiffUtil.ItemCallback<Request>() {
        override fun areItemsTheSame(oldItem: Request, newItem: Request): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Request, newItem: Request): Boolean {
            return oldItem == newItem
        }
    }
}

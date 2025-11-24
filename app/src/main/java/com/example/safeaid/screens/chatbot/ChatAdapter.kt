package com.example.safeaid.screens.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemChatReceiverBinding
import com.example.dermatology.databinding.ItemChatSenderBinding

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENDER = 1
        private const val VIEW_TYPE_RECEIVER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isSender) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENDER -> {
                val binding = ItemChatSenderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SenderViewHolder(binding)
            }
            else -> {
                val binding = ItemChatReceiverBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ReceiverViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SenderViewHolder -> holder.bind(message)
            is ReceiverViewHolder -> holder.bind(message)
        }
    }

    class SenderViewHolder(private val binding: ItemChatSenderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tv.text = message.content
        }
    }

    class ReceiverViewHolder(private val binding: ItemChatReceiverBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        private val medicineSourceAdapter = MedicineSourceAdapter { source ->
        }

        init {
            binding.rcvSources.adapter = medicineSourceAdapter
        }

        fun bind(message: ChatMessage) {
            binding.tv.text = message.content

            // Show sources if available
            if (message.sources != null && message.sources.isNotEmpty()) {
                binding.layoutSources.visibility = android.view.View.VISIBLE
                medicineSourceAdapter.submitList(message.sources)
            } else {
                binding.layoutSources.visibility = android.view.View.GONE
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}

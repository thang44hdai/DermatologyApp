package com.example.safeaid.screens.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dermatology.databinding.ItemConversationBinding
import com.example.safeaid.core.response.Session

class ConversationAdapter(
    private val onItemClick: (Session) -> Unit
) : ListAdapter<Session, ConversationAdapter.ViewHolder>(DiffCallback()) {

    private var selectedSessionId: String? = null

    fun setSelectedSession(sessionId: String?) {
        val oldSelectedId = selectedSessionId
        selectedSessionId = sessionId
        
        currentList.forEachIndexed { index, session ->
            if (session.id == oldSelectedId || session.id == selectedSessionId) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), getItem(position).id == selectedSessionId)
    }

    inner class ViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: Session, isSelected: Boolean) {
            binding.tvConversationTitle.text = session.title ?: "Cuộc trò chuyện"
            
            // Highlight selected conversation
            if (isSelected) {
                binding.conversationItem.setBackgroundResource(com.example.dermatology.R.drawable.bg_conversation_selected)
                binding.tvConversationTitle.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
            } else {
                binding.conversationItem.setBackgroundResource(com.example.dermatology.R.drawable.bg_conversation_normal)
                binding.tvConversationTitle.setTextColor(
                    binding.root.context.getColor(android.R.color.black)
                )
            }
            
            binding.conversationItem.setOnClickListener {
                onItemClick(session)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Session>() {
        override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem == newItem
        }
    }
}

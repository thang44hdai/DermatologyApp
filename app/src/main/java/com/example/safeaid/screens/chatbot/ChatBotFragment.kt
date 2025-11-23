package com.example.safeaid.screens.chatbot

import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.databinding.DrawerChatConversationsBinding
import com.example.dermatology.databinding.FragmentChatBotBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ChatBotFragment : BaseFragment<FragmentChatBotBinding>() {
    private val viewModel: ChatBotViewModel by viewModels()
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var drawerBinding: DrawerChatConversationsBinding

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        setupDrawer()
        setupConversationList()
        loadConversations()
    }

    override fun onInitObserver() {
        viewModel.conversations
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { conversations ->
                conversationAdapter.submitList(conversations)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            findNavController().popBackStack()
        }

        viewBinding.icMenu.setOnDebounceClick {
            if (viewBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                viewBinding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                viewBinding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        drawerBinding.btnNewChat.setOnClickListener {
            viewModel.createNewConversation()
            viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerBinding.edtSearchConversation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchConversations(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupDrawer() {
        val drawerView = viewBinding.drawerContent.root
        drawerBinding = DrawerChatConversationsBinding.bind(drawerView)
    }

    private fun setupConversationList() {
        conversationAdapter = ConversationAdapter { session ->
            viewModel.selectConversation(session)
            viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerBinding.rvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversationAdapter
        }
    }

    private fun loadConversations() {
        viewModel.loadConversations()
    }
}
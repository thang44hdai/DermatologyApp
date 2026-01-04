package com.example.safeaid.screens.chatbot

import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
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
import com.example.safeaid.core.utils.KeyboardUtils
import com.example.safeaid.core.utils.setOnDebounceClick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ChatBotFragment : BaseFragment<FragmentChatBotBinding>() {
    private val viewModel: ChatBotViewModel by viewModels()
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var drawerBinding: DrawerChatConversationsBinding

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        setupDrawer()
        setupConversationList()
        setupChatList()
        loadConversations()

        // Setup hide keyboard on touch outside
        KeyboardUtils.setupHideKeyboardOnTouchOutside(
            this,
            viewBinding.root
        )
    }

    override fun onInitObserver() {
        viewModel.conversations
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { conversations ->
                conversationAdapter.submitList(conversations)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.messages
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { messages ->
                chatAdapter.submitList(messages)
                
                // Show/hide empty state
                if (messages.isEmpty()) {
                    viewBinding.rcv.visibility = android.view.View.GONE
                    viewBinding.layoutEmptyState.visibility = android.view.View.VISIBLE
                } else {
                    viewBinding.rcv.visibility = android.view.View.VISIBLE
                    viewBinding.layoutEmptyState.visibility = android.view.View.GONE
                    
                    withContext(Dispatchers.Main) {
                        delay(100)
                        viewBinding.rcv.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isLoading
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { isLoading ->
                viewBinding.btnSend.isEnabled = !isLoading
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isConnected
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { isConnected ->
                viewBinding.btnSend.isEnabled = isConnected && !viewModel.isLoading.value
                viewBinding.edtInput.isEnabled = isConnected
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.connectionStatus
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { status ->
                if (status != null) {
                    viewBinding.loadingOverlay.visibility = android.view.View.VISIBLE
                    viewBinding.tvLoadingStatus.text = status
                } else {
                    viewBinding.loadingOverlay.visibility = android.view.View.GONE
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.currentSession
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { session ->
                conversationAdapter.setSelectedSession(session?.id)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            // Always navigate back to home (MainScreen)
            findNavController().navigate(com.example.dermatology.R.id.mainScreen)
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
            viewBinding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        drawerBinding.edtSearchConversation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchConversations(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewBinding.btnSend.setOnDebounceClick {
            val message = viewBinding.edtInput.text.toString()
            if (message.isNotBlank()) {
                viewModel.sendMessage(message)
                viewBinding.edtInput.text?.clear()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Vui lòng nhập nội dung trò chuyện",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupDrawer() {
        val drawerView = viewBinding.drawerContent.root
        drawerBinding = DrawerChatConversationsBinding.bind(drawerView)
    }

    private fun setupConversationList() {
        conversationAdapter = ConversationAdapter { session ->
            viewModel.selectConversation(session)
            viewBinding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        drawerBinding.rvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversationAdapter
        }
    }

    private fun setupChatList() {
        chatAdapter = ChatAdapter()
        viewBinding.rcv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun loadConversations() {
        viewModel.loadConversations()
    }
}
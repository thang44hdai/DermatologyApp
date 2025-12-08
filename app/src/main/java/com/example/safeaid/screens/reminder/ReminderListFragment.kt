package com.example.safeaid.screens.reminder

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.databinding.FragmentReminderListBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.reminder.adapter.ReminderItemAdapter
import com.example.safeaid.screens.reminder.viewmodel.ReminderListState
import com.example.safeaid.screens.reminder.viewmodel.ReminderListViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ReminderListFragment : BaseFragment<FragmentReminderListBinding>() {

    private val viewModel: ReminderListViewModel by viewModels()
    private lateinit var reminderAdapter: ReminderItemAdapter
    
    private var currentTab = "active" // "active" or "old"

    override fun isHostFragment(): Boolean = false

    override fun onInit() {
        setupRecyclerView()
        setupTabs()
        
        // Load data
        viewModel.loadReminders()
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderItemAdapter { reminder ->
            // Navigate to detail
            // TODO: Navigate to reminder detail
        }
        
        viewBinding.rcvReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
    }

    private fun setupTabs() {
        viewBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentTab = "active"
                        filterReminders()
                    }
                    1 -> {
                        currentTab = "old"
                        filterReminders()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterReminders() {
        // Filter based on current tab
        viewModel.filterReminders(currentTab)
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                updateUi(state)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateUi(state: com.example.safeaid.core.utils.DataResult<ReminderListState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is ReminderListState.RemindersList -> {
                    reminderAdapter.submitList(data.reminders)
                    
                    // Show empty state if needed
                    viewBinding.tvEmptyState.visibility = if (data.reminders.isEmpty()) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                }
            }
        }
        state?.doIfFailure { error ->
            android.widget.Toast.makeText(
                requireContext(),
                error.message ?: "Có lỗi xảy ra",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }
}

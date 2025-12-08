package com.example.safeaid.screens.reminder

import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentReminderCalendarBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.reminder.adapter.CalendarDayAdapter
import com.example.safeaid.screens.reminder.adapter.ReminderTimeAdapter
import com.example.safeaid.screens.reminder.viewmodel.ReminderCalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ReminderCalendarFragment : BaseFragment<FragmentReminderCalendarBinding>() {

    private val viewModel: ReminderCalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarDayAdapter
    private lateinit var reminderAdapter: ReminderTimeAdapter
    private var isFirstLoad = true

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        setupCalendarRecyclerView()
        setupReminderRecyclerView()
    }

    override fun onInitObserver() {
        viewModel.calendarDays
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { days ->
                calendarAdapter.submitList(days) {
                    // Scroll to selected day (today) after list is updated
                    val selectedIndex = days.indexOfFirst { it.isSelected }
                    if (selectedIndex != -1) {
                        if (isFirstLoad) {
                            // For first load, add extra delay to ensure layout is complete
                            viewBinding.rcvWeekCalendar.postDelayed({
                                scrollToCenter(selectedIndex)
                                isFirstLoad = false
                            }, 100)
                        } else {
                            scrollToCenter(selectedIndex)
                        }
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.reminderTimes
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { reminders ->
                reminderAdapter.submitList(reminders)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isEmpty
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { isEmpty ->
                viewBinding.emptyState.isVisible = isEmpty
                viewBinding.rcvReminders.isVisible = !isEmpty
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }

    private fun setupCalendarRecyclerView() {
        calendarAdapter = CalendarDayAdapter { day ->
            viewModel.selectDay(day)
            // Scroll to selected day when clicked
            val selectedIndex = calendarAdapter.currentList.indexOfFirst { it.date == day.date }
            if (selectedIndex != -1) {
                scrollToCenter(selectedIndex)
            }
        }

        viewBinding.rcvWeekCalendar.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = calendarAdapter
        }
    }

    private fun setupReminderRecyclerView() {
        reminderAdapter = ReminderTimeAdapter { medicine, isChecked ->
            viewModel.onMedicineCheckChanged(medicine, isChecked)
        }

        viewBinding.rcvReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
    }

    private fun scrollToCenter(position: Int) {
        val layoutManager = viewBinding.rcvWeekCalendar.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            // Post to ensure RecyclerView is laid out
            viewBinding.rcvWeekCalendar.post {
                val itemView = it.findViewByPosition(position)
                if (itemView != null) {
                    // Calculate offset to center the item
                    val recyclerViewWidth = viewBinding.rcvWeekCalendar.width
                    val itemWidth = itemView.width
                    val offset = (recyclerViewWidth / 2) - (itemWidth / 2)

                    it.scrollToPositionWithOffset(position, offset)
                } else {
                    // If view not found, use smooth scroll
                    viewBinding.rcvWeekCalendar.smoothScrollToPosition(position)
                }
            }
        }
    }
}

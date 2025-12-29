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
import com.example.safeaid.core.ui.showErrorDialog
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.reminder.adapter.ReminderTimeAdapter
import com.example.safeaid.screens.reminder.adapter.WeekCalendarAdapter
import com.example.safeaid.screens.reminder.adapter.WeekCalendarItem
import com.example.safeaid.screens.reminder.viewmodel.ReminderCalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ReminderCalendarFragment : BaseFragment<FragmentReminderCalendarBinding>() {

    private val viewModel: ReminderCalendarViewModel by viewModels()
    private lateinit var calendarAdapter: WeekCalendarAdapter
    private lateinit var reminderAdapter: ReminderTimeAdapter
    private var isFirstLoad = true

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        setupCalendarRecyclerView()
        setupReminderRecyclerView()
        viewModel.loadWeekCalendar()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning from other screens
        if (!isFirstLoad) {
            viewModel.reloadCurrentWeek()
        }
    }

    override fun onInitObserver() {
        viewModel.viewState.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                it?.doIfFailure {
                    requireContext().showErrorDialog(
                        title = "Lỗi",
                        message = "Lỗi truy cập dữ liệu"
                    )
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.calendarDays
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { days ->
                // Convert to WeekCalendarItem list with navigation buttons
                val items = mutableListOf<WeekCalendarItem>()
                items.add(WeekCalendarItem.NavigationButton(isPrevious = true))
                items.addAll(days.map { WeekCalendarItem.DayItem(it) })
                items.add(WeekCalendarItem.NavigationButton(isPrevious = false))
                
                calendarAdapter.submitList(items) {
                    // Scroll to selected day after list is updated
                    val selectedIndex = days.indexOfFirst { it.isSelected }
                    if (selectedIndex != -1) {
                        if (isFirstLoad) {
                            // For first load, add extra delay to ensure layout is complete
                            viewBinding.rcvWeekCalendar.postDelayed({
                                scrollToCenter(selectedIndex + 1) // +1 because of previous button
                                isFirstLoad = false
                            }, 100)
                        } else {
                            scrollToCenter(selectedIndex + 1) // +1 because of previous button
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
        calendarAdapter = WeekCalendarAdapter(
            onDayClick = { day ->
                viewModel.selectDay(day)
            },
            onPreviousWeekClick = {
                viewModel.loadPreviousWeek()
            },
            onNextWeekClick = {
                viewModel.loadNextWeek()
            }
        )

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
        viewBinding.rcvWeekCalendar.postDelayed({
            val layoutManager = viewBinding.rcvWeekCalendar.layoutManager as? LinearLayoutManager ?: return@postDelayed
            
            // Trick: Assuming screen shows 5 items at once
            // To center an item, we need to show 2 items before it
            // So we scroll to (position - 2) with offset 0
            val targetPosition = maxOf(0, position - 2)
            layoutManager.scrollToPositionWithOffset(targetPosition, 0)
        }, 150)
    }
}

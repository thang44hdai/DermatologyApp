package com.example.safeaid.screens.reminder

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentReminderMainBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReminderMainFragment : BaseFragment<FragmentReminderMainBinding>() {
    
    private lateinit var pagerAdapter: ReminderPagerAdapter
    private var currentPage = 0

    override fun isHostFragment(): Boolean = true

    override fun onInit() {
        setupViewPager()
        setupBottomNavigation()
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        // FAB button to add new reminder
        viewBinding.fab.setOnDebounceClick {
            findNavController().navigate(R.id.action_reminderMainFragment_to_createMedicineReminderFragment)
        }

        // Bottom navigation
        viewBinding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_reminder_calendar -> {
                    viewBinding.viewPager.setCurrentItem(0, true)
                    true
                }
                R.id.nav_reminder_list -> {
                    viewBinding.viewPager.setCurrentItem(1, true)
                    true
                }
                else -> false
            }
        }

        // ViewPager page change listener
        viewBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position

                val menuItemId = when (position) {
                    0 -> R.id.nav_reminder_calendar
                    1 -> R.id.nav_reminder_list
                    else -> R.id.nav_reminder_calendar
                }
                viewBinding.bottomNav.menu.findItem(menuItemId)?.isChecked = true
            }
        })
    }

    private fun setupViewPager() {
        pagerAdapter = ReminderPagerAdapter(this)
        viewBinding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false // Disable swipe
            offscreenPageLimit = 2
        }
    }

    private fun setupBottomNavigation() {
        viewBinding.bottomNav.menu.findItem(R.id.nav_reminder_calendar)?.isChecked = true
    }
}

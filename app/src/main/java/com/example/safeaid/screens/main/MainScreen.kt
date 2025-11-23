package com.example.safeaid.screens.main

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentMainScreenBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainScreen : BaseFragment<FragmentMainScreenBinding>() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var pagerAdapter: MainPagerAdapter

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        setupViewPager()
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    viewBinding.viewPager.setCurrentItem(0, false)
                    true
                }
                R.id.nav_map -> {
                    viewBinding.viewPager.setCurrentItem(1, false)
                    true
                }
                R.id.nav_chat -> {
                    findNavController().navigate(R.id.action_mainScreen_to_chatBotFragment)
                    false // Don't update bottom nav selection
                }
                R.id.nav_profile -> {
                    viewBinding.viewPager.setCurrentItem(2, false)
                    true
                }
                else -> {
                    viewBinding.viewPager.setCurrentItem(0, false)
                    true
                }
            }
        }

        viewBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mainViewModel.currentPage = position

                val menuItemId = when (position) {
                    0 -> R.id.nav_home
                    1 -> R.id.nav_map
                    2 -> R.id.nav_profile
                    else -> R.id.nav_home
                }
                viewBinding.bottomNav.menu.findItem(menuItemId)?.isChecked = true
                
                viewBinding.fab.isVisible = position == 0 || position == 2
            }
        })

        viewBinding.viewPager.setCurrentItem(mainViewModel.currentPage, false)

        viewBinding.fab.setOnDebounceClick {
            findNavController().navigate(R.id.action_mainScreen_to_cameraFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // Restore bottom nav selection when returning from ChatBot
        val menuItemId = when (mainViewModel.currentPage) {
            0 -> R.id.nav_home
            1 -> R.id.nav_map
            2 -> R.id.nav_profile
            else -> R.id.nav_home
        }
        viewBinding.bottomNav.menu.findItem(menuItemId)?.isChecked = true
    }

    private fun setupViewPager() {
        pagerAdapter = MainPagerAdapter(this)
        viewBinding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false // Disable swipe gesture
            offscreenPageLimit = 3 // Keep all fragments in memory
        }
    }
}
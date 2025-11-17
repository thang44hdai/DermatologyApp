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
            val position = when (it.itemId) {
                R.id.nav_home -> 0
                R.id.nav_map -> 1
                R.id.nav_chat -> 2
                R.id.nav_profile -> 3
                else -> 0
            }
            viewBinding.viewPager.setCurrentItem(position, false)
            true
        }

        viewBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mainViewModel.currentPage = position
                
                viewBinding.bottomNav.menu.getItem(position).isChecked = true
                
                viewBinding.fab.isVisible = position == 0 || position == 3
            }
        })

        viewBinding.viewPager.setCurrentItem(mainViewModel.currentPage, false)

        viewBinding.fab.setOnDebounceClick {
            findNavController().navigate(R.id.action_mainScreen_to_cameraFragment)
        }
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
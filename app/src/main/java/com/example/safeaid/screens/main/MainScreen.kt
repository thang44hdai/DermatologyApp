package com.example.safeaid.screens.main

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentMainScreenBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.camera.CameraFragment
import com.example.safeaid.screens.chatbot.ChatBotFragment
import com.example.safeaid.screens.finger.FingerFragment
import com.example.safeaid.screens.home.HomeFragment
import com.example.safeaid.screens.map.MapFragment
import com.example.safeaid.screens.profile.ProfileFragment
import dagger.hilt.android.AndroidEntryPoint

class MainScreen : BaseFragment<FragmentMainScreenBinding>() {
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        replaceFragment(HomeFragment())
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
        viewBinding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    mainViewModel.currentPage = 0
                    viewBinding.fab.isVisible = true
                }

                R.id.nav_map -> {
                    replaceFragment(MapFragment())
                    mainViewModel.currentPage = 1
                    viewBinding.fab.isVisible = true
                }

                R.id.nav_chat -> {
                    replaceFragment(ChatBotFragment())
                    mainViewModel.currentPage = 2
                    viewBinding.fab.isVisible = false
                }

                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    mainViewModel.currentPage = 3
                    viewBinding.fab.isVisible = true
                }

                else -> {
                    replaceFragment(HomeFragment())
                    mainViewModel.currentPage = 0
                    viewBinding.fab.isVisible = true
                }
            }
            true
        }

        viewBinding.bottomNav.selectedItemId = when (mainViewModel.currentPage) {
            0 -> R.id.nav_home
            1 -> R.id.nav_map
            2 -> R.id.nav_chat
            3 -> R.id.nav_profile
            else -> R.id.nav_home
        }

        viewBinding.fab.setOnDebounceClick {
            findNavController().navigate(R.id.action_mainScreen_to_cameraFragment)
        }
    }

    fun replaceFragment(fr: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.nav_host_main, fr)
            .commit()
    }

}
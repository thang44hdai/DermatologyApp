package com.example.safeaid.screens.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentMainScreenBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.screens.camera.CameraFragment
import com.example.safeaid.screens.finger.FingerFragment
import com.example.safeaid.screens.home.HomeFragment

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
                }

                R.id.nav_scan -> {
                    replaceFragment(CameraFragment())
                    mainViewModel.currentPage = 1
                }

                R.id.nav_chat -> {
                    replaceFragment(FingerFragment())
                    mainViewModel.currentPage = 4
                }

                else -> {
                    replaceFragment(HomeFragment())
                    mainViewModel.currentPage = 0
                }
            }
            true
        }

        viewBinding.bottomNav.selectedItemId = when (mainViewModel.currentPage) {
            0 -> R.id.nav_home
            1 -> R.id.nav_scan
            2 -> R.id.nav_chat
            else -> R.id.nav_home
        }
    }

    fun replaceFragment(fr: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.nav_host_main, fr)
            .commit()
    }

}
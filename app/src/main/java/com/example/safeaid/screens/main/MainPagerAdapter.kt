package com.example.safeaid.screens.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.safeaid.screens.home.HomeFragment
import com.example.safeaid.screens.map.MapFragment
import com.example.safeaid.screens.profile.ProfileFragment

class MainPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> MapFragment()
            2 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}

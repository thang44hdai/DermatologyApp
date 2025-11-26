package com.example.safeaid.screens.profile

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentProfileBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.home.HomeViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
    }

    override fun onInitObserver() {
        homeViewModel._user
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { data ->
                viewBinding.tvName.text = data.fullname
                viewBinding.tvEmail.text = data.email
                data.avatarUrl?.let { url ->
                    com.bumptech.glide.Glide.with(requireContext())
                        .load(url)
                        .circleCrop()
                        .into(viewBinding.imvAvatar)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.btnHistory.setOnDebounceClick {
            findNavController().navigate(R.id.action_mainScreen_to_historyFragment)
        }

        viewBinding.btnReminder.setOnDebounceClick {
            findNavController().navigate(R.id.action_mainScreen_to_reminderCalendarFragment)
        }
    }

}
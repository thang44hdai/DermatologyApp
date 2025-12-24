package com.example.safeaid.screens.profile

import android.content.Intent
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentProfileBinding
import com.example.safeaid.MainActivity
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.ui.showConfirmDialog
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.authenication.viewmodel.LoginViewModel
import com.example.safeaid.screens.home.HomeViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

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
                    Glide.with(requireContext())
                        .load(url)
                        .circleCrop()
                        .into(viewBinding.imvAvatar)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        // Profile edit button
        viewBinding.btnEditProfile.setOnDebounceClick {
            android.widget.Toast.makeText(
                requireContext(),
                "Chức năng đang phát triển",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        // History button
        viewBinding.btnHistory.setOnDebounceClick {
            findNavController().navigate(R.id.action_mainScreen_to_historyFragment)
        }

        // Reminder button
        viewBinding.btnReminder.setOnDebounceClick {
            findNavController().navigate(R.id.action_mainScreen_to_reminderMainFragment)
        }

        // Skin care button
        viewBinding.btnSkinCare.setOnDebounceClick {
            android.widget.Toast.makeText(
                requireContext(),
                "Thiền dưỡng tâm - Chức năng đang phát triển",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        // Logout button (hidden but keep logic)
        viewBinding.btnLogout.setOnDebounceClick {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        requireContext().showConfirmDialog(
            title = "Đăng xuất",
            message = "Bạn có chắc chắn muốn đăng xuất?",
            onConfirm = {
                loginViewModel.clearToken()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Đã đăng xuất",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onCancel = {},
            confirmText = "Đăng xuất",
            cancelText = "Hủy"
        )
    }
}
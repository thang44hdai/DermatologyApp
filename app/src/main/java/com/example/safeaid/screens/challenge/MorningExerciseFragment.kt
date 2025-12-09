package com.example.safeaid.screens.challenge

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentMorningExerciseBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.challenge.viewmodel.MorningExerciseState
import com.example.safeaid.screens.challenge.viewmodel.MorningExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MorningExerciseFragment : BaseFragment<FragmentMorningExerciseBinding>() {

    private val viewModel: MorningExerciseViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi"))

    override fun isHostFragment(): Boolean = false

    override fun onInit() {
        setupUI()
        viewModel.loadChallengeData()
    }

    private fun setupUI() {
        // Set current date
        viewBinding.tvDate.text = dateFormat.format(Date())
        
        // Check if can check-in today
        updateCheckInButton()
    }

    private fun updateCheckInButton() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Check-in available from 6:00 AM to 10:00 AM
        val canCheckIn = hour in 6..9
        
        viewBinding.btnCheckIn.isEnabled = canCheckIn
        
        if (!canCheckIn) {
            if (hour < 6) {
                viewBinding.tvCheckInHint.text = "Check-in mở cửa lúc 6:00 sáng"
            } else {
                viewBinding.tvCheckInHint.text = "Hết giờ check-in (6:00 - 10:00 sáng)"
            }
        } else {
            viewBinding.tvCheckInHint.text = "Bạn đã tập thể dục sáng nay chưa?"
        }
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                updateUi(state)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateUi(state: com.example.safeaid.core.utils.DataResult<MorningExerciseState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is MorningExerciseState.ChallengeData -> {
                    // Update streak
                    viewBinding.tvStreakCount.text = "${data.streak}"
                    
                    // Update total days
                    viewBinding.tvTotalDays.text = "${data.totalDays} ngày"
                    
                    // Update this week
                    viewBinding.tvThisWeek.text = "${data.thisWeekCount}/7 ngày"
                    
                    // Update progress
                    val progress = (data.thisWeekCount * 100) / 7
                    viewBinding.progressWeek.progress = progress
                    
                    // Update check-in status
                    if (data.checkedInToday) {
                        viewBinding.btnCheckIn.text = "✓ Đã check-in hôm nay"
                        viewBinding.btnCheckIn.isEnabled = false
                        viewBinding.tvCheckInHint.text = "Tuyệt vời! Hẹn gặp bạn vào sáng mai"
                    }
                    
                    // Update motivational quote
                    viewBinding.tvMotivation.text = data.motivationalQuote
                }
                
                is MorningExerciseState.CheckInSuccess -> {
                    showCheckInSuccessDialog(data.newStreak)
                    viewModel.loadChallengeData() // Reload data
                }
            }
        }
        
        state?.doIfFailure { error ->
            android.widget.Toast.makeText(
                requireContext(),
                error.message ?: "Có lỗi xảy ra",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }

        viewBinding.btnCheckIn.setOnDebounceClick {
            viewModel.checkIn()
        }

        viewBinding.btnHistory.setOnDebounceClick {
            // TODO: Navigate to history screen
            android.widget.Toast.makeText(
                requireContext(),
                "Lịch sử - Chức năng đang phát triển",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        viewBinding.btnExercises.setOnDebounceClick {
            showExerciseSuggestions()
        }
    }

    private fun showCheckInSuccessDialog(newStreak: Int) {
        val message = when {
            newStreak == 1 -> "Tuyệt vời! Bạn đã bắt đầu hành trình của mình!"
            newStreak < 7 -> "Tuyệt vời! Bạn đã duy trì được $newStreak ngày liên tiếp!"
            newStreak < 30 -> "Xuất sắc! $newStreak ngày liên tiếp! Bạn đang làm rất tốt!"
            else -> "Không thể tin được! $newStreak ngày liên tiếp! Bạn là huyền thoại!"
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎉 Check-in thành công!")
            .setMessage(message)
            .setPositiveButton("Tuyệt vời!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showExerciseSuggestions() {
        val exercises = listOf(
            "🏃 Chạy bộ 15-20 phút",
            "🧘 Yoga buổi sáng 10 phút",
            "💪 Plank 3 hiệp x 30 giây",
            "🤸 Jumping jacks 3 hiệp x 20 lần",
            "🦵 Squat 3 hiệp x 15 lần",
            "💪 Push-up 3 hiệp x 10 lần",
            "🧘 Stretching toàn thân 10 phút"
        )

        val message = exercises.joinToString("\n")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💪 Gợi ý bài tập buổi sáng")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}

package com.example.safeaid.screens.challenge

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.SystemClock
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentRunningChallengeBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.challenge.viewmodel.RunningChallengeState
import com.example.safeaid.screens.challenge.viewmodel.RunningChallengeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RunningChallengeFragment : BaseFragment<FragmentRunningChallengeBinding>(),
    SensorEventListener {

    private val viewModel: RunningChallengeViewModel by viewModels()
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null

    private var isRunning = false
    private var isPaused = false
    private var initialStepCount = 0
    private var currentSteps = 0
    private var sessionSteps = 0
    private var startTime = 0L
    private var pausedTime = 0L
    private var elapsedTime = 0L

    private val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi"))

    private val activityPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupStepCounter()
        } else {
            Toast.makeText(requireContext(), "Cần quyền để đo bước chân", Toast.LENGTH_SHORT).show()
        }
    }

    override fun isHostFragment(): Boolean = false

    override fun onInit() {
        setupUI()
        viewModel.loadChallengeData()
        checkPermissionAndSetup()
    }

    private fun setupUI() {
        viewBinding.tvDate.text = dateFormat.format(Date())
        updateRunningUI(false)
    }

    private fun checkPermissionAndSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    setupStepCounter()
                }

                else -> {
                    activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        } else {
            setupStepCounter()
        }
    }

    private fun setupStepCounter() {
        sensorManager =
            requireContext().getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(requireContext(), "Thiết bị không hỗ trợ đếm bước", Toast.LENGTH_SHORT)
                .show()
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

    private fun updateUi(state: com.example.safeaid.core.utils.DataResult<RunningChallengeState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is RunningChallengeState.ChallengeData -> {
                    viewBinding.tvTotalDistance.text = String.format("%.2f km", data.totalDistance)
                    viewBinding.tvTotalRuns.text = "${data.totalRuns} lần"
                    viewBinding.tvWeeklyGoal.text =
                        "${data.weeklyDistance}/${data.weeklyGoalDistance} km"

                    val progress = ((data.weeklyDistance / data.weeklyGoalDistance) * 100).toInt()
                    viewBinding.progressWeekly.progress = progress

                    viewBinding.tvTodaySteps.text = "${data.todaySteps} bước"
                    viewBinding.tvTodayDistance.text = String.format("%.2f km", data.todayDistance)
                    viewBinding.tvTodayCalories.text = "${data.todayCalories} kcal"
                }

                is RunningChallengeState.SaveSuccess -> {
                    Toast.makeText(
                        requireContext(),
                        "Đã lưu hoạt động chạy bộ!",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadChallengeData()
                }
            }
        }

        state?.doIfFailure { error ->
            Toast.makeText(requireContext(), error.message ?: "Có lỗi xảy ra", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }

        viewBinding.btnStartStop.setOnDebounceClick {
            if (!isRunning) {
                startRunning()
            } else {
                stopRunning()
            }
        }

        viewBinding.btnPauseResume.setOnDebounceClick {
            if (isPaused) {
                resumeRunning()
            } else {
                pauseRunning()
            }
        }

        viewBinding.btnHistory.setOnDebounceClick {
            showHistory()
        }

        viewBinding.btnAchievements.setOnDebounceClick {
            showAchievements()
        }
    }

    private fun startRunning() {
        if (stepSensor == null) {
            Toast.makeText(
                requireContext(),
                "Không thể bắt đầu - thiết bị không hỗ trợ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        isRunning = true
        isPaused = false
        sessionSteps = 0
        startTime = SystemClock.elapsedRealtime()

        sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)

        updateRunningUI(true)
        startTimer()

        Toast.makeText(requireContext(), "Bắt đầu chạy! Cố lên!", Toast.LENGTH_SHORT).show()
    }

    private fun pauseRunning() {
        isPaused = true
        pausedTime = SystemClock.elapsedRealtime()
        sensorManager?.unregisterListener(this)

        viewBinding.btnPauseResume.text = "Tiếp tục"
//        viewBinding.btnPauseResume.setIconResource(R.drawable.ic_next)
    }

    private fun resumeRunning() {
        isPaused = false
        val pauseDuration = SystemClock.elapsedRealtime() - pausedTime
        startTime += pauseDuration

        sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)

        viewBinding.btnPauseResume.text = "Tạm dừng"
//        viewBinding.btnPauseResume.setIconResource(R.drawable.ic_time)
    }

    private fun stopRunning() {
        isRunning = false
        isPaused = false

        sensorManager?.unregisterListener(this)

        elapsedTime = SystemClock.elapsedRealtime() - startTime

        showRunSummary()
        updateRunningUI(false)
    }

    private fun updateRunningUI(running: Boolean) {
        if (running) {
            viewBinding.btnStartStop.text = "Kết thúc"
//            viewBinding.btnStartStop.setIconResource(R.drawable.ic_close)
            viewBinding.btnStartStop.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.negative)
            viewBinding.btnPauseResume.visibility = android.view.View.VISIBLE
            viewBinding.cardRunning.visibility = android.view.View.VISIBLE
        } else {
            viewBinding.btnStartStop.text = "Bắt đầu chạy"
//            viewBinding.btnStartStop.setIconResource(R.drawable.ic_navigate)
            viewBinding.btnStartStop.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.primary)
            viewBinding.btnPauseResume.visibility = android.view.View.GONE
            viewBinding.cardRunning.visibility = android.view.View.GONE

            viewBinding.tvSessionSteps.text = "0"
            viewBinding.tvSessionDistance.text = "0.00"
            viewBinding.tvSessionTime.text = "00:00"
            viewBinding.tvSessionCalories.text = "0"
        }
    }

    private fun startTimer() {
        viewBinding.tvSessionTime.postDelayed(object : Runnable {
            override fun run() {
                if (isRunning && !isPaused) {
                    val elapsed = SystemClock.elapsedRealtime() - startTime
                    val seconds = (elapsed / 1000).toInt()
                    val minutes = seconds / 60
                    val secs = seconds % 60

                    viewBinding.tvSessionTime.text = String.format("%02d:%02d", minutes, secs)
                    viewBinding.tvSessionTime.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialStepCount == 0) {
                initialStepCount = event.values[0].toInt()
            }

            currentSteps = event.values[0].toInt()
            sessionSteps = currentSteps - initialStepCount

            updateSessionStats()
        }
    }

    private fun updateSessionStats() {
        viewBinding.tvSessionSteps.text = sessionSteps.toString()

        val distance = sessionSteps * 0.0008
        viewBinding.tvSessionDistance.text = String.format("%.2f", distance)

        val calories = (sessionSteps * 0.04).toInt()
        viewBinding.tvSessionCalories.text = calories.toString()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun showRunSummary() {
        val distance = sessionSteps * 0.0008
        val calories = (sessionSteps * 0.04).toInt()
        val timeMinutes = (elapsedTime / 1000 / 60).toInt()

        val message = """
            Tuyệt vời! Bạn đã hoàn thành:
            
            Bước chân: $sessionSteps bước
            Quãng đường: ${String.format("%.2f", distance)} km
            Thời gian: $timeMinutes phút
            Calories: $calories kcal
            
            Tiếp tục phát huy nhé!
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hoàn thành!")
            .setMessage(message)
            .setPositiveButton("Lưu hoạt động") { _, _ ->
                viewModel.saveRunningSession(sessionSteps, distance, timeMinutes.toLong(), calories)
                resetSession()
            }
            .setNegativeButton("Bỏ qua") { _, _ ->
                resetSession()
            }
            .show()
    }

    private fun resetSession() {
        initialStepCount = 0
        currentSteps = 0
        sessionSteps = 0
        startTime = 0L
        pausedTime = 0L
        elapsedTime = 0L
    }

    private fun showAchievements() {
        val achievements = viewModel.getAchievements()
        
        val message = buildString {
            achievements.forEach { achievement ->
                val status = if (achievement.unlocked) "✅" else "🔒"
                append("$status ${achievement.title}\n")
                append("   ${achievement.description}\n\n")
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🏆 Thành tích")
            .setMessage(message.trim())
            .setPositiveButton("Đóng", null)
            .show()
    }
    
    private fun showHistory() {
        val history = viewModel.getHistory().take(10) // Show last 10 sessions
        
        if (history.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có lịch sử chạy bộ", Toast.LENGTH_SHORT).show()
            return
        }
        
        val message = buildString {
            history.forEachIndexed { index, session ->
                append("${index + 1}. ${session.date}\n")
                append("   ${session.steps} bước • ${String.format("%.2f", session.distance)} km\n")
                append("   ${session.durationMinutes} phút • ${session.calories} kcal\n\n")
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Lịch sử chạy bộ")
            .setMessage(message.trim())
            .setPositiveButton("Đóng", null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (isRunning && !isPaused) {
            pauseRunning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }
}

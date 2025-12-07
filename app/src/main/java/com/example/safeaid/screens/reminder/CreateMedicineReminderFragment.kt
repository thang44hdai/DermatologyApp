package com.example.safeaid.screens.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentCreateMedicineReminderBinding
import com.example.safeaid.core.request.CreateReminderRequest
import com.example.safeaid.core.request.TimeSchedule
import com.example.safeaid.core.response.MedicineResponse
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.Utils
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.reminder.adapter.DayItem
import com.example.safeaid.screens.reminder.adapter.DaySelectorAdapter
import com.example.safeaid.screens.reminder.adapter.SelectionChipAdapter
import com.example.safeaid.screens.reminder.adapter.TimeReminderAdapter
import com.example.safeaid.screens.reminder.viewmodel.CreateMedicineReminderViewModel
import com.example.safeaid.screens.reminder.viewmodel.CreateReminderState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateMedicineReminderFragment : BaseFragment<FragmentCreateMedicineReminderBinding>() {

    private val viewModel: CreateMedicineReminderViewModel by viewModels()
    private lateinit var unitAdapter: SelectionChipAdapter
    private lateinit var timeReminderAdapter: TimeReminderAdapter

    private val units = listOf("Viên", "Xịt", "Ống", "ml", "Miếng", "Liều", "Gói", "Giọt")

    private var selectedUnit = "Viên"
    private var selectedMedicine: MedicineResponse? = null
    private var selectedDays = mutableListOf<String>()
    private var startDate = Calendar.getInstance()
    private var endDate = Calendar.getInstance()

    private val timePeriodItems = mutableListOf<TimePeriodItem>()

    private val dateFormat = SimpleDateFormat("d 'Tháng' M", Locale("vi"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("vi"))

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        setupUnitRecyclerView()
        setupTimeReminderRecyclerView()
        setupRadioButtons()
        updateDateDisplays()
        setupNoteCounter()

        // Load medicines from API
        viewModel.loadMedicines()
    }

    private fun setupUnitRecyclerView() {
        unitAdapter = SelectionChipAdapter(units) { unit, _ ->
            selectedUnit = unit
            // Update all time period items with new unit
            updateAllTimePeriodUnits(unit)
        }

        viewBinding.rcvUnit.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = unitAdapter
        }
    }

    private fun setupTimeReminderRecyclerView() {
        timeReminderAdapter = TimeReminderAdapter()
        timeReminderAdapter.setOnDeleted {
            timePeriodItems.removeIf { item -> it.id == item.id }
            timeReminderAdapter.submitList(timePeriodItems.toList())
        }

        viewBinding.rcvTimePeriod.adapter = timeReminderAdapter
    }

    private fun updateAllTimePeriodUnits(newUnit: String) {
        // Update unit for all items
        val updatedList = timePeriodItems.map { item ->
            item.copy(unit = newUnit)
        }
        timePeriodItems.clear()
        timePeriodItems.addAll(updatedList)
        // Submit new list to trigger adapter update
        timeReminderAdapter.submitList(updatedList)
    }

    private fun setupRadioButtons() {
        // Set custom colors for radio buttons
        val colorStateList = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary),
                androidx.core.content.ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_neutral_4
                )
            )
        )

        viewBinding.radioTruoc.buttonTintList = colorStateList
        viewBinding.radioSau.buttonTintList = colorStateList
    }

    private fun updateDateDisplays() {
        viewBinding.tvStartDate.text = dateFormat.format(startDate.time)
        viewBinding.tvEndDate.text = dateFormat.format(endDate.time)
    }

    private fun setupNoteCounter() {
        viewBinding.edtNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                viewBinding.root.findViewById<android.widget.TextView>(R.id.tv_note_counter)?.text =
                    "$length/500"
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                updateUi(state)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateUi(state: com.example.safeaid.core.utils.DataResult<CreateReminderState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is CreateReminderState.MedicinesList -> {
                    setupMedicineDropdown(data.medicines)
                }
                is CreateReminderState.CreateSuccess -> {
                    Toast.makeText(
                        requireContext(),
                        "Đã thêm nhắc nhở uống thuốc thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
            }
        }
        state?.doIfFailure { error ->
            Toast.makeText(
                requireContext(), 
                error.message ?: "Có lỗi xảy ra",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupMedicineDropdown(medicines: List<MedicineResponse>) {
        viewBinding.tvMedicineDropdown.setOnClickListener {
            showMedicineDialog(medicines)
        }
    }

    private fun showMedicineDialog(medicines: List<MedicineResponse>) {
        val medicineNames = medicines.map { it.name ?: "Unknown" }.toTypedArray()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Chọn thuốc")
        builder.setItems(medicineNames) { dialog, which ->
            selectedMedicine = medicines.getOrNull(which)
            selectedMedicine?.let { medicine ->
                viewBinding.tvMedicineDropdown.text = medicine.name
                viewBinding.tvMedicineDropdown.setTextColor(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black)
                )
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()

        // Set cancel button color to red
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red_600)
        )
    }

    override fun onInitListener() {
        viewBinding.btnBack.setOnDebounceClick {
            findNavController().popBackStack()
        }

        // Start date picker
        viewBinding.tvStartDate.setOnClickListener {
            showDatePicker(startDate) { calendar ->
                startDate = calendar
                updateDateDisplays()
            }
        }

        // End date picker
        viewBinding.tvEndDate.setOnClickListener {
            showDatePicker(endDate) { calendar ->
                endDate = calendar
                updateDateDisplays()
            }
        }

        // Frequency selector
        viewBinding.tvFrequency.setOnClickListener {
            showFrequencyDialog()
        }

        // Add time button
        viewBinding.btnAddTime.setOnClickListener {
            addNewTimePeriod()
        }

        // Add medicine button
        viewBinding.btnAddMedicine.setOnClickListener {
            saveMedicineReminder()
        }
    }

    private fun addNewTimePeriod() {
        val newItem = TimePeriodItem(
            time = "",
            dosage = "1",
            unit = selectedUnit
        )
        timePeriodItems.add(newItem)
        timeReminderAdapter.submitList(timePeriodItems.toList())
    }

    private fun showFrequencyDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_frequency_selector, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val rcvDays =
            dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rcv_days)
        val btnCancel = dialogView.findViewById<android.widget.TextView>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<android.widget.TextView>(R.id.btn_confirm)

        val days = listOf(
            DayItem("all", "Tất cả các ngày", isAllDays = true),
            DayItem("mon", "Thứ Hai"),
            DayItem("tue", "Thứ Ba"),
            DayItem("wed", "Thứ Tư"),
            DayItem("thu", "Thứ Năm"),
            DayItem("fri", "Thứ Sáu"),
            DayItem("sat", "Thứ Bảy"),
            DayItem("sun", "Chủ Nhật")
        )

        var currentSelection = selectedDays.toMutableList()
        var dayItems = days.map { day ->
            day.copy(isSelected = currentSelection.contains(day.id))
        }

        val adapter = DaySelectorAdapter {}
        adapter.setOnClick { clickedDay ->
            if (clickedDay.isAllDays) {
                // If "All days" clicked, select only it
                currentSelection = if (currentSelection.contains("all")) {
                    mutableListOf()
                } else {
                    mutableListOf("all")
                }
            } else {
                // If specific day clicked, remove "all" and toggle this day
                currentSelection.remove("all")
                if (currentSelection.contains(clickedDay.id)) {
                    currentSelection.remove(clickedDay.id)
                } else {
                    currentSelection.add(clickedDay.id)
                }
            }
            dayItems = days.map { day ->
                day.copy(isSelected = currentSelection.contains(day.id))
            }
            adapter.submitList(dayItems)
        }
        // Update UI immediately after click
        dayItems = days.map { day ->
            day.copy(isSelected = currentSelection.contains(day.id))
        }
        adapter.submitList(dayItems)

        rcvDays.adapter = adapter
        adapter.submitList(dayItems)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            selectedDays = currentSelection
            updateFrequencyDisplay()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateFrequencyDisplay() {
        val displayText = when {
            selectedDays.isEmpty() -> "Chọn tần suất"
            selectedDays.contains("all") -> "Tất cả các ngày"
            else -> {
                val dayNames = mapOf(
                    "mon" to "Thứ Hai",
                    "tue" to "Thứ Ba",
                    "wed" to "Thứ Tư",
                    "thu" to "Thứ Năm",
                    "fri" to "Thứ Sáu",
                    "sat" to "Thứ Bảy",
                    "sun" to "Chủ Nhật"
                )
                selectedDays.mapNotNull { dayNames[it] }.joinToString(", ")
            }
        }
        viewBinding.tvFrequency.text = displayText
        viewBinding.tvFrequency.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                requireContext(),
                if (selectedDays.isEmpty()) R.color.gray_600 else R.color.black
            )
        )
    }

    private fun showDatePicker(currentDate: Calendar, onDateSelected: (Calendar) -> Unit) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar)
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveMedicineReminder() {
        // Validate
        if (selectedMedicine == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn thuốc", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (timePeriodItems.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng thêm ít nhất một thời gian uống thuốc", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedDays.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn tần suất sử dụng thuốc", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if all time periods have time selected
        val hasEmptyTime = timePeriodItems.any { it.time.isEmpty() }
        if (hasEmptyTime) {
            Toast.makeText(requireContext(), "Vui lòng chọn giờ cho tất cả thời gian uống thuốc", Toast.LENGTH_SHORT).show()
            return
        }

        // Build request
        val medicineName = selectedMedicine?.name ?: ""
        val medicineId = selectedMedicine?.id
        val note = viewBinding.edtNote.text.toString()
        val isBeforeMeal = viewBinding.radioTruoc.isChecked
        val isNotificationEnabled = viewBinding.switchNotification.isChecked
        
        // Convert time periods to TimeSchedule
        val times = timePeriodItems.map { item ->
            TimeSchedule(
                time = item.time,
                period = Utils.getPeriodFromTime(item.time),
                dosage = item.dosage
            )
        }
        
        // Determine frequency
        val frequency = when {
            selectedDays.contains("all") -> "daily"
            else -> "specific_days"
        }
        
        // Convert days to API format (0=Monday, 6=Sunday)
        val daysOfWeek = if (frequency == "specific_days") {
            selectedDays.mapNotNull { dayId ->
                when (dayId) {
                    "mon" -> 0
                    "tue" -> 1
                    "wed" -> 2
                    "thu" -> 3
                    "fri" -> 4
                    "sat" -> 5
                    "sun" -> 6
                    else -> null
                }
            }
        } else null
        
        // Format dates
        val startDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate.time)
        val endDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate.time)
        
        val request = CreateReminderRequest(
            medicineId = medicineId,
            medicineName = medicineName,
            dosage = selectedMedicine?.dosage,
            unit = selectedUnit,
            mealTiming = if (isBeforeMeal) "before_meal" else "after_meal",
            frequency = frequency,
            times = times,
            daysOfWeek = daysOfWeek,
            startDate = startDateStr,
            endDate = endDateStr,
            isNotificationEnabled = isNotificationEnabled,
            notes = note.ifEmpty { null }
        )
        
        // Call API
        viewModel.createReminder(request)
    }
}

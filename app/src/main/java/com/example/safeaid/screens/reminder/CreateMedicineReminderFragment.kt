package com.example.safeaid.screens.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.Editable
import android.text.TextWatcher
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentCreateMedicineReminderBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.setOnDebounceClick
import com.example.safeaid.screens.reminder.adapter.SelectionChipAdapter
import com.example.safeaid.screens.reminder.adapter.TimePeriodAdapter
import java.text.SimpleDateFormat
import java.util.*

class CreateMedicineReminderFragment : BaseFragment<FragmentCreateMedicineReminderBinding>() {

    private lateinit var unitAdapter: SelectionChipAdapter
    private lateinit var timePeriodAdapter: TimePeriodAdapter
    
    private val units = listOf("Viên", "Xịt", "Ống", "ml", "Miếng", "Liều", "Gói", "Giọt")
    private val timePeriods = listOf("Sáng", "Trưa", "Chiều", "Tối")
    
    private var selectedUnit = "Viên"
    private var selectedTimePeriods = mutableListOf<String>()
    private var startDate = Calendar.getInstance()
    private var endDate = Calendar.getInstance()
    private var selectedTime = Calendar.getInstance()
    
    private val dateFormat = SimpleDateFormat("d 'Tháng' M", Locale("vi"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("vi"))

    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        setupUnitRecyclerView()
        setupTimePeriodRecyclerView()
        setupRadioButtons()
        updateDateDisplays()
        updateTimeDisplay()
        setupNoteCounter()
    }

    private fun setupUnitRecyclerView() {
        unitAdapter = SelectionChipAdapter(units) { unit, _ ->
            selectedUnit = unit
        }
        
        viewBinding.rcvUnit.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = unitAdapter
        }
    }

    private fun setupTimePeriodRecyclerView() {
        timePeriodAdapter = TimePeriodAdapter(timePeriods) { selectedItems ->
            selectedTimePeriods = selectedItems.toMutableList()
        }
        
        viewBinding.rcvTimePeriod.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = timePeriodAdapter
        }
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
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_neutral_4)
            )
        )
        
        viewBinding.radioTruoc.buttonTintList = colorStateList
        viewBinding.radioSau.buttonTintList = colorStateList
    }

    private fun updateDateDisplays() {
        viewBinding.tvStartDate.text = dateFormat.format(startDate.time)
        viewBinding.tvEndDate.text = dateFormat.format(endDate.time)
    }

    private fun updateTimeDisplay() {
        viewBinding.tvTime.text = timeFormat.format(selectedTime.time)
    }

    private fun setupNoteCounter() {
        viewBinding.edtNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                viewBinding.root.findViewById<android.widget.TextView>(R.id.tv_note_counter)?.text = "$length/500"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onInitObserver() {
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

        // Time picker
        viewBinding.tvTime.setOnClickListener {
            showTimePicker()
        }

        // Add medicine button
        viewBinding.btnAddMedicine.setOnClickListener {
            saveMedicineReminder()
        }
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

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun saveMedicineReminder() {
        // Get all values
        val medicineName = viewBinding.spinnerMedicine.selectedItem?.toString() ?: ""
        val dosage = viewBinding.edtDosage.text.toString()
        val note = viewBinding.edtNote.text.toString()
        val isBeforeMeal = viewBinding.radioTruoc.isChecked
        val isNotificationEnabled = viewBinding.switchNotification.isChecked
        
        // Validate
        if (medicineName.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Vui lòng chọn thuốc", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedTimePeriods.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Vui lòng chọn thời gian uống thuốc", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Save to database or send to API
        android.widget.Toast.makeText(
            requireContext(),
            "Đã thêm nhắc nhở uống thuốc: $medicineName",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        findNavController().popBackStack()
    }
}
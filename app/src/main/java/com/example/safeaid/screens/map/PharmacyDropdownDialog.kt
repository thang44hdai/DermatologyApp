package com.example.safeaid.screens.map

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermatology.R
import com.example.dermatology.databinding.DialogPharmacyDropdownBinding
import com.example.safeaid.core.response.PharmacyResponse
import com.example.safeaid.screens.map.adapter.PharmacyDropdownAdapter
import com.example.safeaid.screens.map.utils.PharmacyUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PharmacyDropdownDialog : BottomSheetDialogFragment() {
    private var _binding: DialogPharmacyDropdownBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: PharmacyDropdownAdapter
    private var allPharmacies = listOf<PharmacyResponse>()
    private var onPharmacyClickListener: ((PharmacyResponse) -> Unit)? = null
    private var searchEditText: EditText? = null
    private var textWatcher: TextWatcher? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPharmacyDropdownBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchListener()
        
        // Keep EditText visible by not expanding to full screen
        dialog?.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    override fun getTheme(): Int = R.style.NoDimBottomSheet

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun onStart() {
                super.onStart()
                window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)

            val displayMetrics = resources.displayMetrics
            val maxHeight = (displayMetrics.heightPixels * 0.8).toInt()

            sheet.layoutParams.height = maxHeight
            sheet.requestLayout()

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = maxHeight
            behavior.isDraggable = true
        }
    }


    private fun setupRecyclerView() {
        adapter = PharmacyDropdownAdapter { pharmacy ->
            onPharmacyClickListener?.invoke(pharmacy)
        }
        
        binding.rvPharmacies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PharmacyDropdownDialog.adapter
        }
        
        updateList(allPharmacies)
    }

    private fun setupSearchListener() {
        searchEditText?.let { editText ->
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s.toString().trim()
                    val filtered = PharmacyUtils.filterPharmacies(allPharmacies, query)
                    updateList(filtered)
                }
                
                override fun afterTextChanged(s: Editable?) {}
            }
            editText.addTextChangedListener(textWatcher)
            
            // Trigger initial search if there's already text
            val currentText = editText.text.toString().trim()
            if (currentText.isNotEmpty()) {
                val filtered = PharmacyUtils.filterPharmacies(allPharmacies, currentText)
                updateList(filtered)
            }
        }
    }

    private fun updateList(list: List<PharmacyResponse>) {
        if (::adapter.isInitialized) {
            adapter.submitList(list)
            
            // Update title with count
            binding.tvTitle.text = "Kết quả tìm kiếm (${list.size})"
            
            // Show/hide empty state
            if (list.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvPharmacies.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvPharmacies.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Set the EditText to listen for search queries
     */
    fun attachSearchEditText(editText: EditText) {
        searchEditText = editText
        if (_binding != null) {
            setupSearchListener()
        }
    }

    /**
     * Set all pharmacies data
     */
    fun setAllPharmacies(list: List<PharmacyResponse>) {
        allPharmacies = list
        updateList(list)
    }

    fun setOnPharmacyClickListener(listener: (PharmacyResponse) -> Unit) {
        onPharmacyClickListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove text watcher to prevent memory leak
        searchEditText?.removeTextChangedListener(textWatcher)
        textWatcher = null
        searchEditText = null
        _binding = null
    }
}

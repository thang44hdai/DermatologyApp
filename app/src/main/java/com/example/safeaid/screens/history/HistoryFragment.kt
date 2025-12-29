package com.example.safeaid.screens.history

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermatology.R
import com.example.dermatology.databinding.FragmentHistoryBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.core.utils.DataResult
import com.example.safeaid.core.utils.doIfFailure
import com.example.safeaid.core.utils.doIfSuccess
import com.example.safeaid.core.utils.setOnDebounceClick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun isHostFragment(): Boolean {
        return false
    }

    override fun onInit() {
        viewModel.getHistoryList()
        adapter = HistoryAdapter(onItemClick = { scan ->
            val bundle = Bundle()
            bundle.putSerializable(DetailHistoryFragment.ARG, scan)
            findNavController().navigate(
                R.id.action_historyFragment_to_detailHistoryFragment,
                bundle
            )
        })
        viewBinding.rcv.adapter = adapter
    }

    override fun onInitObserver() {
        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> updateUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onInitListener() {
        viewBinding.icBack.setOnDebounceClick {
            findNavController().popBackStack()
        }
    }

    private fun updateUi(state: DataResult<HistoryState>?) {
        state?.doIfSuccess { data ->
            when (data) {
                is HistoryState.HistoryList -> {
                    adapter.updateData(data.data.scans)
                }

                else -> {}
            }
        }
        state?.doIfFailure {
            Toast.makeText(
                requireContext(),
                it.message,
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

}
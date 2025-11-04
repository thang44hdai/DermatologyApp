package com.example.safeaid.screens.pharmacy

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dermatology.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PharmacyDetailFragment : Fragment() {

    companion object {
        fun newInstance() = PharmacyDetailFragment()
    }

    private val viewModel: PharmacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pharmacy_detail, container, false)
    }
}
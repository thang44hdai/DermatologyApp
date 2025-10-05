package com.example.safeaid.screens.quiz

import androidx.fragment.app.activityViewModels
import com.example.dermatology.databinding.FragmentQuizCategoryBinding
import com.example.safeaid.core.ui.BaseFragment
import com.example.safeaid.screens.quiz.viewmodel.QuizCategoryViewModel

class QuizCategoryFragment : BaseFragment<FragmentQuizCategoryBinding>() {
    private val viewModel: QuizCategoryViewModel by activityViewModels()
    override fun isHostFragment(): Boolean {
        return true
    }

    override fun onInit() {
        viewModel.getQuizCategory()
    }

    override fun onInitObserver() {
    }

    override fun onInitListener() {
    }
}
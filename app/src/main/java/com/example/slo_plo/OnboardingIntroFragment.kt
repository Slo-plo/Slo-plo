package com.example.slo_plo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.slo_plo.databinding.FragmentOnboardingIntroBinding

class OnboardingIntroFragment : Fragment() {

    private var _binding: FragmentOnboardingIntroBinding? = null
    private val binding get() = _binding!!

    private val pages = OnboardingIntro.entries.toTypedArray()
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updatePage()

        binding.btnIntroContinue.setOnClickListener {
            if (currentPage < pages.lastIndex) {
                currentPage++
                updatePage()
            } else {
                findNavController().navigate(R.id.action_onboardingIntroFragment_to_onboardingGuideFragment)
            }
        }

        binding.btnIntroSkip.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingIntroFragment_to_onboardingGuideFragment)
        }
    }

    private fun updatePage() {
        val page = pages[currentPage]
        binding.tvIntroContent.text = page.text
        binding.ivIntroContent.setImageResource(page.imageResId)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

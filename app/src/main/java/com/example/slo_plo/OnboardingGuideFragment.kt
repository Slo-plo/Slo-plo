package com.example.slo_plo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.slo_plo.databinding.FragmentOnboardingGuideBinding

class OnboardingGuideFragment : Fragment() {

    private var _binding: FragmentOnboardingGuideBinding? = null
    private val binding get() = _binding!!

    private val pages = OnboardingGuide.entries.toTypedArray()
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updatePage()

        binding.btnGuideContinue.setOnClickListener {
            if (currentPage < pages.lastIndex) {
                currentPage++
                updatePage()
            } else {
                // 온보딩 종료와 플래그 저장
                requireActivity().getSharedPreferences("slo_plo_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("isFirstLaunch", false).apply()

                findNavController().navigate(R.id.action_onboardingGuideFragment_to_homeFragment)
            }
        }

        binding.btnGuideSkip.setOnClickListener {
            // 홈 화면 이동과 플래그 저장
            requireActivity().getSharedPreferences("slo_plo_prefs", Context.MODE_PRIVATE)
                .edit().putBoolean("isFirstLaunch", false).apply()

            findNavController().navigate(R.id.action_onboardingGuideFragment_to_homeFragment)
        }
    }

    private fun updatePage() {
        val page = pages[currentPage]
        binding.tvGuideTitle.text = page.title
        binding.tvGuideContent.text = page.content
        binding.ivGuideContent.setImageResource(page.imageResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

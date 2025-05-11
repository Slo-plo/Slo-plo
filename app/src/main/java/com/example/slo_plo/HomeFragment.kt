package com.example.slo_plo

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.slo_plo.databinding.FragmentHomeBinding
import com.example.slo_plo.databinding.FragmentPloggingBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.btnHomePlogging.setOnClickListener {
            // PloggingFragment로 이동
            findNavController().navigate(R.id.action_homeFragment_to_ploggingFragment)
        }

        // 날짜 설정
        val currentDate = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA)
        binding.itemHomePlogging.tvHomeDate.text = currentDate.format(dateFormatter)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

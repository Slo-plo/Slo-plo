package com.example.slo_plo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.slo_plo.databinding.FragmentLogDetailBinding
import com.example.slo_plo.model.LogRecord

class LogDetailFragment : Fragment() {

    private var _binding: FragmentLogDetailBinding? = null
    private val binding get() = _binding!!

    private var logRecord: LogRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        logRecord = arguments?.getSerializable("logRecord") as? LogRecord

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("LogDetailFragment", "✅ onViewCreated() 진입")

        binding.btnLogBack.setOnClickListener {
            Log.d("LogDetailFragment", "뒤로가기 버튼 클릭됨")
            findNavController().popBackStack()
        }

        logRecord?.let { log ->
            binding.tvLogTitle.text = log.title
            binding.tvLogDate.text = log.dateId
            // 등등...
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

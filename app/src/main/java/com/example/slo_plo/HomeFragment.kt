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
import com.example.slo_plo.utils.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isToday = true  // 오늘의 플로깅이 기본값

    private val userId = FirebaseAuth.getInstance().currentUser?.uid  // 유저 아이디

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

        loadPloggingData()

        binding.itemHomePlogging.btnRecordChange.setOnClickListener {
            isToday = !isToday
            loadPloggingData()
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        loadPloggingData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 이동 거리 포맷 함수
    private fun formatDistance(meters: Double): String {
        val km = meters / 1000

        return if (meters <= 100) {
            "0.1"
        } else {
            val rounded = String.format(Locale.US, "%.1f", km).toDouble()
            if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
        }
    }

    // 시간 포맷 함수 (오늘의 플로깅)
    private fun formatTimeForToday(minutes: Int): Pair<String, String> {
        return if (minutes < 60) {
            minutes.toString() to "분"
        } else {
            val hours = minutes.toDouble() / 60.0
            val rounded = String.format(Locale.US, "%.1f", hours).toDouble()
            val text = if (rounded % 1.0 == 0.0) {
                // 1.0시간이라도 반드시 1시간으로 표시
                rounded.toInt().toString()
            } else {
                rounded.toString()
            }
            text to "시간"
        }
    }

    // 시간 포맷 함수 (역대의 플로깅)
    private fun formatTimeForHistory(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours == 0 -> "총 시간: ${remainingMinutes}분"
            remainingMinutes == 0 -> "총 시간: ${hours}시간 0분"  // 0분도 표시
            else -> "총 시간: ${hours}시간 ${remainingMinutes}분"
        }
    }

    private fun loadPloggingData() {
        if (isToday) {
            binding.itemHomePlogging.tvHomeTitle.text = "오늘의 플로깅"
            binding.itemHomePlogging.layoutPloggingRecordToday.root.visibility = View.VISIBLE
            binding.itemHomePlogging.layoutPloggingRecordHistory.root.visibility = View.GONE

            if (userId != null) {
                FirestoreRepository.loadLogRecordsForDate(userId, LocalDate.now()) { records ->
                    val totalTime = records.sumOf { it.time }
                    val totalDistance = records.sumOf { it.distance }
                    val totalTrash = records.sumOf { it.trashCount }

                    val displayDistance = formatDistance(totalDistance)
                    val (displayTime, timeUnit) = formatTimeForToday(totalTime)

                    binding.itemHomePlogging.layoutPloggingRecordToday.tvValueRecordTime.text =
                        displayTime
                    binding.itemHomePlogging.layoutPloggingRecordToday.tvUnitRecordTime.text =
                        timeUnit
                    binding.itemHomePlogging.layoutPloggingRecordToday.tvValueRecordDist.text =
                        displayDistance
                    binding.itemHomePlogging.layoutPloggingRecordToday.tvValueRecordTrash.text =
                        "$totalTrash"
                }
            }

        } else {
            binding.itemHomePlogging.tvHomeTitle.text = "역대의 플로깅"
            binding.itemHomePlogging.layoutPloggingRecordToday.root.visibility = View.GONE
            binding.itemHomePlogging.layoutPloggingRecordHistory.root.visibility = View.VISIBLE

            if (userId != null) {
                FirestoreRepository.loadAllLogRecords(userId) { records ->
                    val totalCount = records.size
                    val totalTime = records.sumOf { it.time }
                    val totalDistance = records.sumOf { it.distance }
                    val totalTrash = records.sumOf { it.trashCount }

                    val displayDistance = formatDistance(totalDistance)
                    val displayTime = formatTimeForHistory(totalTime)
                    val displayTrash = "수집한 쓰레기: ${totalTrash}개"

                    binding.itemHomePlogging.layoutPloggingRecordHistory.tvValueRecordCount.text =
                        "$totalCount"
                    binding.itemHomePlogging.layoutPloggingRecordHistory.tvTotalTime.text =
                        displayTime
                    binding.itemHomePlogging.layoutPloggingRecordHistory.tvTotalDistance.text =
                        "이동 거리: ${displayDistance}km"
                    binding.itemHomePlogging.layoutPloggingRecordHistory.tvTotalTrash.text =
                        displayTrash
                }
            }
        }
    }
}

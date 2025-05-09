package com.example.slo_plo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.slo_plo.databinding.DayViewBinding
import com.example.slo_plo.databinding.FragmentJournalBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    // 표시할 날짜
    private val greenDates = setOf(
        LocalDate.of(2025, 4, 2),
        LocalDate.of(2025, 4, 4),
        LocalDate.of(2025, 4, 5),
        LocalDate.of(2025, 4, 11),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 날짜 셀 그리기 정의
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.bind(day)
            }
        }

        // 캘린더 표시 범위 설정(4월)
        binding.calendarView.setup(
            YearMonth.of(2025, 4),
            YearMonth.of(2025, 4),
            DayOfWeek.SUNDAY
        )
        // 기본 스크롤 위치 설정
        binding.calendarView.scrollToDate(LocalDate.of(2025, 4, 11))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        private val itemBinding = DayViewBinding.bind(view)                 // 날짜 셀(binding)
        private val parentBinding get() = this@JournalFragment.binding      // 프래그먼트 전체(binding)

        fun bind(day: CalendarDay) {
            val date = day.date

            if (day.position == DayPosition.MonthDate) {
                // 날짜 텍스트 표시
                itemBinding.dayText.text = date.dayOfMonth.toString()

                // 클로버 아이콘 표시 여부
                itemBinding.dayIcon.visibility = View.VISIBLE
                itemBinding.dayIcon.setImageResource(
                    if (date in greenDates) R.drawable.ic_unit_24 else 0
                )

                // 날짜 클릭 시, 프래그먼트 하단 텍스트뷰들 업데이트
                view.setOnClickListener {
                    if (date == LocalDate.of(2025, 4, 11)) {
                        parentBinding.logDateText.text = "2025.04.11 (금) 2.5km"
                        parentBinding.logSummaryText.text = "담배꽁초 50개 주움"
                        parentBinding.logStartPlaceText.text = "📍 반포한강공원 | 50분"
                        parentBinding.logTrashText.text = "오늘의 총 쓰레기: 50개"
                    } else {
                        parentBinding.logDateText.text = "$date 기록 없음"
                        parentBinding.logSummaryText.text = ""
                        parentBinding.logStartPlaceText.text = ""
                        parentBinding.logTrashText.text = ""
                    }
                }

            } else {
                // 빈 칸 처리 (달력 앞/뒤 날짜)
                itemBinding.dayIcon.visibility = View.INVISIBLE
                itemBinding.dayText.text = ""
                view.setOnClickListener(null)
            }
        }
    }

}
package com.example.slo_plo

import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import androidx.core.view.children
import com.example.slo_plo.databinding.DayViewBinding
import com.example.slo_plo.databinding.FragmentJournalBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: LocalDate? = null


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

        // 뒤로가기 버튼 클릭 시 이전 화면으로 이동
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 상세 보기 버튼 연결
        binding.buttonDetail.setOnClickListener {
            // 실제 이동은 나중에 구현
            Toast.makeText(requireContext(), "상세보기 화면 연결 예정입니다.", Toast.LENGTH_SHORT).show()
        }

        // 날짜 셀 그리기 정의
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.bind(day)
            }
        }


        // 현재 월을 기준으로 이전 12개월부터 이후 12개월까지 설정
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)

        binding.calendarView.setup(startMonth, endMonth, DayOfWeek.SUNDAY)
        binding.calendarView.scrollToMonth(currentMonth)

        // 요일 헤더 표시
        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                val daysOfWeek = DayOfWeek.values()
                container.monthTitle.text = "${month.yearMonth.year}년 ${month.yearMonth.monthValue}월"
                container.titlesContainer.children
                    .map { it as TextView }
                    .forEachIndexed { index, textView ->
                        val dayOfWeek = daysOfWeek[index]
                        val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        textView.text = title
                    }
            }
        }
    }

    // 헤더 바인딩용 ViewContainer
    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer: LinearLayout = view.findViewById(R.id.dayOfWeekRow)
        val monthTitle: TextView = view.findViewById(R.id.textMonthTitle)
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

                // 클릭된 날짜 표시
                if (selectedDate == date) {
                    itemBinding.dayText.setBackgroundResource(R.drawable.bg_selected_day)
                    itemBinding.dayText.setTextColor(Color.WHITE)
                } else {
                    itemBinding.dayText.background = null
                    itemBinding.dayText.setTextColor(Color.BLACK)
                }


                // 아이콘 표시 여부
                itemBinding.dayIcon.visibility = View.VISIBLE
                itemBinding.dayIcon.setImageResource(
                    if (date in greenDates) R.drawable.ic_unit_24 else 0
                )

                // 클릭 이벤트
                view.setOnClickListener {
                    if (selectedDate != date) {
                        val oldDate = selectedDate
                        selectedDate = date
                        binding.calendarView.notifyDateChanged(date)
                        oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                    }

                    // 요약 텍스트 갱신
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
                }
            } else {
                itemBinding.dayText.text = ""
                itemBinding.dayIcon.visibility = View.INVISIBLE
                itemBinding.dayText.background = null
                view.setOnClickListener(null)
            }
        }
    }

}
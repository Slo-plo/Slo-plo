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
import android.util.Log
import androidx.core.view.children
import com.example.slo_plo.databinding.DayViewBinding
import com.example.slo_plo.databinding.FragmentJournalBinding
import com.google.firebase.firestore.FirebaseFirestore
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
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.example.slo_plo.model.LogRecord

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: LocalDate? = null

    private val today = LocalDate.now()

    private val firestoreDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

//    data class LogRecord(
//        val distance: Double = 0.0,
//        val time: Int = 0,
//        val trashCount: Int = 0,
//        val title: String = "",
//        val address: String = ""
//    )

    // 표시할 날짜
    private val greenDates = mutableSetOf<LocalDate>()

    private fun loadDatesForCalendarIcons() {
        FirebaseFirestore.getInstance().collection("plogging_logs").get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val date = try {
                        LocalDate.parse(doc.id, firestoreDateFormatter)  // 점(.) 포맷으로 파싱
                    } catch (e: Exception) {
                        null
                    }
                    date?.let { greenDates.add(it) }
                }
                binding.calendarView.notifyCalendarChanged()
            }
    }
    // 날짜 형식
    private fun formatDateWithDayOfWeek(date: LocalDate): String {
        val formattedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.KOREAN)
        return "$formattedDate ($dayOfWeek)"
    }


    // Firestore에서 날짜별 기록 조회
    private fun loadLogRecord(date: LocalDate, callback: (LogRecord?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docId = date.format(firestoreDateFormatter)

        db.collection("plogging_logs").document(docId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val record = doc.toObject(LogRecord::class.java)
                    callback(record)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                callback(null)
            }
    }

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
                // 일요일부터 시작하는 순서로 재정렬
                val daysOfWeek = listOf(
                    DayOfWeek.SUNDAY,
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                )

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
        loadDatesForCalendarIcons()

        // 요약 뷰 초기 메시지
        binding.logDateText.text = "기록을 확인할 날짜를 선택해 주세요"
        binding.logTitleText.text = ""
        binding.logStartPlaceText.text = ""
        binding.logTrashText.text = ""

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

                when {
                    selectedDate == date -> {
                        itemBinding.dayText.setBackgroundResource(R.drawable.bg_selected_day)
                        itemBinding.dayText.setTextColor(Color.WHITE)
                    }
                    date == today -> {
                        itemBinding.dayText.setBackgroundResource(R.drawable.bg_today_day)
                        itemBinding.dayText.setTextColor(Color.BLACK)
                    }
                    else -> {
                        itemBinding.dayText.background = null
                        itemBinding.dayText.setTextColor(Color.BLACK)
                    }
                }

                // 날짜 클릭 시 이벤트 설정
                view.setOnClickListener {
                    // 선택 날짜 변경
                    if (selectedDate != date) {
                        val oldDate = selectedDate
                        selectedDate = date
                        binding.calendarView.notifyDateChanged(date)
                        oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                    }

                    // 요약 데이터 불러오기
                    loadLogRecord(date) { record ->
                        if (record != null) {
                            parentBinding.logDateText.text = "${formatDateWithDayOfWeek(date)} ${record.distance}km"
                            parentBinding.logTitleText.text = record.title
                            parentBinding.logStartPlaceText.text = "📍 ${record.startAddress} | ${record.time}"
                            parentBinding.logTrashText.text = "쓰레기 개수: ${record.trashCount}개"
                        } else {
                            parentBinding.logDateText.text = "${formatDateWithDayOfWeek(date)} 기록 없음"
                            parentBinding.logTitleText.text = ""
                            parentBinding.logStartPlaceText.text = ""
                            parentBinding.logTrashText.text = ""
                        }
                    }
                }

                // 아이콘 표시 여부
                itemBinding.dayIcon.visibility = View.VISIBLE
                itemBinding.dayIcon.setImageResource(
                    if (date in greenDates) R.drawable.ic_unit_24 else 0
                )

            } else {
                itemBinding.dayText.text = ""
                itemBinding.dayIcon.visibility = View.INVISIBLE
                itemBinding.dayText.background = null
                view.setOnClickListener(null)
            }
        }
    }
}
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
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.slo_plo.utils.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: LocalDate? = null

    private val today = LocalDate.now()

    private val firestoreDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    private lateinit var auth: FirebaseAuth
    private var uid: String? = null

    private var currentLogRecords: List<LogRecord> = emptyList()


    // 표시할 날짜
    private val greenDates = mutableSetOf<LocalDate>()

    // 날짜별로 저장된 일지 데이터에서 dateId 추출 → greenDates에 추가 → 아이콘 표시
    private fun loadDatesForCalendarIcons() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("plogging_logs")
            .get()
            .addOnSuccessListener { snaps ->
                greenDates.clear()
                snaps.documents.forEach { doc ->
                    val dateStr = doc.getString("dateId")  // 저장된 필드에서 dateId 읽기
                    val date = runCatching {
                        LocalDate.parse(dateStr, firestoreDateFormatter)
                    }.getOrNull()
                    date?.let { greenDates.add(it) }
                }
                Log.d("JournalFragment", "Loaded dates: $greenDates")
                binding.calendarView.notifyCalendarChanged()
            }
    }
    // 날짜 형식(yyyy.mm.dd (요일))
    private fun formatDateWithDayOfWeek(date: LocalDate): String {
        val formattedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.KOREAN)
        return "$formattedDate ($dayOfWeek)"
    }



    // Firestore에서 날짜별 기록 조회
    private fun loadLogRecord(date: LocalDate, cb: (LogRecord?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("plogging_logs")
            .whereEqualTo("dateId", dateStr)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val firstRecord = querySnapshot.documents.firstOrNull()?.toObject(LogRecord::class.java)
                cb(firstRecord)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                cb(null)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 이미 로그인되어 있으면 바로 uid 설정, 아니라면 익명 로그인
        auth.currentUser?.let {
            uid = it.uid
            uid?.let { loadDatesForCalendarIcons() }
        } ?: run {
            auth.signInAnonymously()
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        uid = auth.currentUser?.uid
                        Log.d("JournalFragment", "익명 로그인 성공, uid=$uid")
                        uid?.let { loadDatesForCalendarIcons() }
                    } else {
                        Log.e("JournalFragment", "익명 로그인 실패", task.exception)
                        Toast.makeText(requireContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // 일지 작성 후 돌아왔을 때 갱신 트리거
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("needsRefresh")
            ?.observe(viewLifecycleOwner) { needs ->
                if (needs == true) {
                    uid?.let { loadDatesForCalendarIcons() }
                    findNavController().currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("needsRefresh")
                }
            }

        // 뒤로가기 버튼 클릭 시 이전 화면으로 이동
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 목록 버튼 클릭 시 일지 모아보기 화면으로 이동
        binding.buttonLogList.setOnClickListener {
            findNavController().navigate(R.id.action_journal_to_logList)
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

        // 요약 뷰 초기 메시지 + ViewPager 숨김
        binding.logDateText.text = "기록을 확인할 날짜를 선택해 주세요"
        binding.logViewPager.visibility = View.GONE  // ← ViewPager 초기 비노출

        //날짜 아이콘 표시용 초기 불러오기
        loadDatesForCalendarIcons()

    }

    // 요일 헤더 ViewHolder
    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer: LinearLayout = view.findViewById(R.id.dayOfWeekRow)
        val monthTitle: TextView = view.findViewById(R.id.textMonthTitle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 다시 화면 돌아왔을 때 날짜 리프레시
    override fun onResume() {
        super.onResume()
        uid?.let { loadDatesForCalendarIcons() }
    }

    // 날짜 셀의 ViewHolder
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

                // 날짜 클릭 시 기록 불러오기 + ViewPager 연결
                view.setOnClickListener {
                    // 선택 날짜 변경
                    if (selectedDate != date) {
                        val oldDate = selectedDate
                        selectedDate = date
                        binding.calendarView.notifyDateChanged(date)
                        oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                    }

                    uid?.let { user ->
                        FirestoreRepository.loadLogRecordsForDate(user, date) { records ->
                            if (records.isEmpty()) {
                                binding.logDateText.text = "기록 없음"
                                binding.logViewPager.visibility = View.GONE
                            } else {
                                binding.logDateText.text = "${records.size}개의 기록이 있습니다"

                                // 상세보기 버튼 클릭 시 LogDetailFragment로 이동
                                val adapter = LogSummaryPagerAdapter(records) { selectedRecord ->
                                    val bundle = Bundle().apply {
                                        putSerializable("logRecord", selectedRecord)
                                    }
                                    findNavController().navigate(R.id.logDetailFragment, bundle)
                                }


                                // ViewPager 연결
                                binding.logViewPager.adapter = adapter
                                binding.logViewPager.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                // 아이콘 표시 여부
                if (date in greenDates) {
                    itemBinding.dayIcon.visibility = View.VISIBLE
                    itemBinding.dayIcon.setImageResource(R.drawable.ic_unit_24)
                } else {
                    itemBinding.dayIcon.visibility = View.INVISIBLE
                }
            } else {
                // 이번 달 외 날짜
                itemBinding.dayText.text = ""
                itemBinding.dayIcon.visibility = View.INVISIBLE
                itemBinding.dayText.background = null
                view.setOnClickListener(null)
            }
        }
    }
}
package com.example.slo_plo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.slo_plo.databinding.FragmentVolunteerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior

class VolunteerFragment : Fragment() {

    private var _binding: FragmentVolunteerBinding? = null
    private val binding get() = _binding!!

    private lateinit var recomendVolunteerAdapter: RecomendVolunteerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVolunteerBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnFindLocation.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_location, null)

            // 화면 높이를 가져오기
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val sheetHeight = (screenHeight * 0.75).toInt() // 화면의 4분의 3 높이

            dialog.setContentView(dialogView)

            // BottomSheetBehavior 커스터마이징
            val bottomSheet = dialog.delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isHideable = false
                behavior.skipCollapsed = true
                // 높이를 화면의 4분의 3로 설정
                it.layoutParams.height = sheetHeight
                it.requestLayout()

                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(sheet: View, newState: Int) {
                        // 드래그로 내려가도 닫히지 않게 유지
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(sheet: View, offset: Float) {}
                })
            }

            // 리스트 연결
            val regionList = dialogView.findViewById<ListView>(R.id.list_region)
            val subRegionList = dialogView.findViewById<ListView>(R.id.list_subregion)
            val btnSelect = dialogView.findViewById<Button>(R.id.btn_select_region)
            val btnClose = dialogView.findViewById<TextView>(R.id.tv_region_close)

            val regions = listOf("서울", "경기", "인천", "강원", "대전/충청", "대구", "부산/울산", "경상", "광주/전라")
            val regionMap = mapOf(
                "서울" to listOf("강남", "강변", "건대입구", "구로", "노원구", "대학로", "동대문", "동촌", "명동", "미아", "버전"),
                "경기" to listOf("수원", "성남", "용인")
                // 생략 가능
            )

            val regionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, regions)
            regionList.adapter = regionAdapter

            regionList.setOnItemClickListener { _, _, position, _ ->
                val selected = regions[position]
                val subRegions = regionMap[selected] ?: emptyList()
                val subAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, subRegions)
                subRegionList.adapter = subAdapter
            }

            btnClose.setOnClickListener { dialog.dismiss() }
            btnSelect.setOnClickListener {
                // 선택된 지역 처리 로직
                dialog.dismiss()
            }

            dialog.show()
        }



        // 리사이클러뷰 설정
        recomendVolunteerAdapter = RecomendVolunteerAdapter(getVolunteerList())
        binding.recyclerViewRecommendVolunteer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recomendVolunteerAdapter

            // 리사이클러뷰의 데이터 아이템 사이에 구분선 추가
            val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }

        return view
    }

    // 더미 데이터 생성
    private fun getVolunteerList(): List<RecomendVolunteer> {
        return listOf(
            RecomendVolunteer("길거리 청소 봉사", "도심 거리 청소를 함께해요!", "노원구", "5월 12일"),
            RecomendVolunteer("노인복지센터 지원", "말벗이 되어 드리고 식사 보조도 해요.", "강남구", "6월 12일"),
            RecomendVolunteer("지역 아동센터 미술 수업", "아이들과 창의력 넘치는 시간을 보내요.", "노원구", "5월 12일"),
            RecomendVolunteer("동물 보호소 봉사", "유기동물들과 산책하며 돌봐줘요.", "노원구", "5월 12일"),
            RecomendVolunteer("환경 보호 캠페인", "자연을 지키는 활동에 참여해요.", "노원구", "5월 12일"),
            RecomendVolunteer("이웃 지원 봉사", "독거노인 분들에게 필요한 물품을 전달해요.", "노원구", "5월 12일"),
            RecomendVolunteer("청소년 멘토링", "청소년들에게 진로 상담을 해줘요.", "노원구", "5월 12일"),
            RecomendVolunteer("해변 청소 봉사", "해변에서 쓰레기를 청소해요.", "노원구", "5월 12일")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

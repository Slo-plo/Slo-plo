package com.example.slo_plo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.slo_plo.databinding.FragmentVolunteerBinding
import com.example.slo_plo.databinding.BottomSheetLocationBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior

class VolunteerFragment : Fragment() {

    private var _binding: FragmentVolunteerBinding? = null
    private val binding get() = _binding!!

    private lateinit var recommendVolunteerAdapter: RecommendVolunteerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVolunteerBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnSharePage.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, InstaShareFragment()) // Fragment 교체
                .addToBackStack(null) // Back Stack에 추가
                .commit() // 트랜잭션 실행
        }

        // 'btnFindLocation' 버튼 클릭 리스너 설정
        binding.btnFindLocation.setOnClickListener {

            // 바텀시트 다이얼로그 생성
            val dialog = BottomSheetDialog(requireContext())
            val sheetBinding = BottomSheetLocationBinding.inflate(layoutInflater)
            val dialogView = sheetBinding.root

            // 화면 높이의 75%로 바텀시트 높이 설정
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val sheetHeight = (screenHeight * 0.75).toInt()

            dialog.setContentView(dialogView)

            // 바텀시트 내부 뷰 속성 조정
            val bottomSheet = dialog.delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isHideable = false
                behavior.skipCollapsed = true
                it.layoutParams.height = sheetHeight
                it.requestLayout()

                // 드래그로 닫히지 않도록 상태 감시
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(sheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    override fun onSlide(sheet: View, offset: Float) {}
                })
            }

            // 지역 리스트 정의 및 맵핑
            val regions = listOf("서울", "경기", "인천", "강원", "대전/충청", "대구", "부산/울산", "경상", "광주/전라")
            val regionMap = mapOf(
                "서울" to listOf("강남", "강변", "건대입구", "구로", "노원구", "대학로", "동대문", "동촌", "명동", "미아", "버전"),
                "경기" to listOf("수원", "성남", "용인")
            )

            // 선택된 지역 및 세부 지역 저장용 변수
            var selectedRegion: String? = null
            var selectedSubRegion: String? = null

            // 첫 번째 리스트뷰에 지역 데이터 연결
            val regionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, regions)
            sheetBinding.listRegion.adapter = regionAdapter

            // 지역을 선택했을 때 서브 지역 리스트뷰 갱신
            sheetBinding.listRegion.setOnItemClickListener { _, _, position, _ ->
                selectedRegion = regions[position]
                val subRegions = regionMap[selectedRegion] ?: emptyList()
                val subAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, subRegions)
                sheetBinding.listSubregion.adapter = subAdapter
            }

            // onCreateView 안에서 selectedPosition을 초기화
            var selectedPosition: Int? = null

            // 지역 클릭 리스너에서 선택된 지역의 위치를 저장
            sheetBinding.listRegion.setOnItemClickListener { _, _, position, _ ->
                selectedRegion = regions[position]
                selectedPosition = position  // 선택된 위치 저장
                val subRegions = regionMap[selectedRegion] ?: emptyList()
                val subAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, subRegions)
                sheetBinding.listSubregion.adapter = subAdapter
            }

            // 서브 지역 선택 처리
            sheetBinding.listSubregion.setOnItemClickListener { _, view, position, _ ->
                // 선택된 아이템의 배경색을 어두운 색으로 변경
                view.setBackgroundColor(Color.parseColor("#F0F0F0"))

                // 이전 선택된 항목의 배경을 리셋
                val previousSelectedPosition = sheetBinding.listSubregion.getTag(R.id.selected_position) as? Int
                if (previousSelectedPosition != null) {
                    val previousSelectedView = sheetBinding.listSubregion.getChildAt(previousSelectedPosition)
                    previousSelectedView?.setBackgroundColor(Color.WHITE)
                }

                // 현재 선택된 항목의 배경을 어두운 색으로 설정
                sheetBinding.listSubregion.setTag(R.id.selected_position, position)
                selectedSubRegion = sheetBinding.listSubregion.adapter.getItem(position) as? String
            }


            // 'X 창 닫기' 텍스트 클릭 시 바텀시트 닫기
            sheetBinding.tvRegionClose.setOnClickListener { dialog.dismiss() }

            // '지역 선택' 버튼 클릭 시
            sheetBinding.btnSelectRegion.setOnClickListener {
                if (selectedRegion != null && selectedSubRegion != null) {
                    val fullRegion = "$selectedRegion $selectedSubRegion"

                    val fragment = RegionVolunteerFragment().apply {
                        arguments = Bundle().apply {
                            putString("region", fullRegion)
                        }
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()

                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            // 다이얼로그 표시
            dialog.show()
        }

        // 리사이클러뷰 설정
        recommendVolunteerAdapter = RecommendVolunteerAdapter(getVolunteerList())
        binding.recyclerViewRecommendVolunteer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendVolunteerAdapter

            val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }

        return view
    }

    private fun getVolunteerList(): List<RecommendVolunteer> {
        return listOf(
            RecommendVolunteer("길거리 청소 봉사", "도심 거리 청소를 함께해요!", "노원구", "5월 12일"),
            RecommendVolunteer("노인복지센터 지원", "말벗이 되어 드리고 식사 보조도 해요.", "강남구", "6월 12일"),
            RecommendVolunteer("지역 아동센터 미술 수업", "아이들과 창의력 넘치는 시간을 보내요.", "노원구", "5월 12일"),
            RecommendVolunteer("동물 보호소 봉사", "유기동물들과 산책하며 돌봐줘요.", "노원구", "5월 12일"),
            RecommendVolunteer("환경 보호 캠페인", "자연을 지키는 활동에 참여해요.", "노원구", "5월 12일"),
            RecommendVolunteer("이웃 지원 봉사", "독거노인 분들에게 필요한 물품을 전달해요.", "노원구", "5월 12일"),
            RecommendVolunteer("청소년 멘토링", "청소년들에게 진로 상담을 해줘요.", "노원구", "5월 12일"),
            RecommendVolunteer("해변 청소 봉사", "해변에서 쓰레기를 청소해요.", "노원구", "5월 12일")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

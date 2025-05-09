package com.example.slo_plo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.slo_plo.databinding.FragmentVolunteerBinding

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

        // 지역 선택 버튼 클릭 이벤트
        binding.btnFindLocation.setOnClickListener {
            Toast.makeText(requireContext(), "지역 선택 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
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

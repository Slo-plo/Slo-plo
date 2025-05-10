package com.example.slo_plo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slo_plo.databinding.FragmentRegionVolunteerBinding

class RegionVolunteerFragment : Fragment() {

    private var _binding: FragmentRegionVolunteerBinding? = null
    private val binding get() = _binding!!
    private lateinit var recommendVolunteerAdapter: RecommendVolunteerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegionVolunteerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 동작 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = VolunteerFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .commit()
            }
        })

        // Arguments로 전달된 region 값을 받음
        val region = arguments?.getString("region") ?: "지역명"
        binding.btnSelectRegion.text = region

        // 전달받은 지역에 맞는 봉사활동 리스트 필터링
        val filteredList = getVolunteerList().filter { it.location == region }

        // 리사이클러뷰 설정
        recommendVolunteerAdapter = RecommendVolunteerAdapter(getVolunteerList())
        binding.recyclerViewResionVolunteer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendVolunteerAdapter

            val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getVolunteerList(): List<RecommendVolunteer> {
        return listOf(
            RecommendVolunteer("길거리 청소 봉사", "도심 거리 청소를 함께해요!", "노원구", "5월 12일"),
            RecommendVolunteer("노인복지센터 지원", "말벗이 되어 드리고 식사 보조도 해요.", "노원구", "6월 12일"),
            RecommendVolunteer("지역 아동센터 미술 수업", "아이들과 창의력 넘치는 시간을 보내요.", "노원구", "5월 12일"),
            RecommendVolunteer("동물 보호소 봉사", "유기동물들과 산책하며 돌봐줘요.", "노원구", "5월 12일"),
            RecommendVolunteer("환경 보호 캠페인", "자연을 지키는 활동에 참여해요.", "노원구", "5월 12일"),
            RecommendVolunteer("이웃 지원 봉사", "독거노인 분들에게 필요한 물품을 전달해요.", "노원구", "5월 12일"),
            RecommendVolunteer("청소년 멘토링", "청소년들에게 진로 상담을 해줘요.", "노원구", "5월 12일"),
            RecommendVolunteer("해변 청소 봉사", "해변에서 쓰레기를 청소해요.", "노원구", "5월 12일")
        )
    }
}

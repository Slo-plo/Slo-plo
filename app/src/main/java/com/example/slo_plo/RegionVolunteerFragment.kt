package com.example.slo_plo

import android.os.Bundle
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegionVolunteerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPressed()

        val region = arguments?.getString("region") ?: "지역명"
        binding.btnSelectRegion.text = region

        setupRecyclerView(region)
    }

    private fun handleBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, VolunteerFragment())
                    .commit()
            }
        })
    }

    private fun setupRecyclerView(region: String) {
        val filteredList = getVolunteerList().filter { it.location == region }

        // empty view 처리
        if (filteredList.isEmpty()) {
            binding.recyclerViewResionVolunteer.visibility = View.GONE
            binding.tvEmptyMessage.visibility = View.VISIBLE // "아직 등록된 봉사활동이 없어요" 텍스트뷰
        } else {
            binding.recyclerViewResionVolunteer.visibility = View.VISIBLE
            binding.tvEmptyMessage.visibility = View.GONE
        }

        recommendVolunteerAdapter = RecommendVolunteerAdapter(filteredList)

        binding.recyclerViewResionVolunteer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendVolunteerAdapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getVolunteerList(): List<RecommendVolunteer> {
        return listOf(
            RecommendVolunteer(
                "[강남구자원봉사센터-공감그린스토리] 같이 함께하는 강남구플로깅로드(오프라인)_쓰레기줍기 환경정화",
                "강남구자원봉사센터에서는 탄소중립 실천을 위한 일환으로 강남구 플로깅 로드 프로젝트를 진행합니다. " +
                        "강남구 플로깅 로드는 '이타서울'웹사이트을 활용한 모바일 플로깅(쓰레기줍기) 활동으로 기록된 정보는 강남구 플로깅 로드를 제작하는데에 활용됩니다.",
                "강남구",
                "2025.05.09 ~ 2025.07.11",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3281268"
            ),
            RecommendVolunteer(
                "[강남구자원봉사센터]강남구플로깅로드_강남구 내 모바일 데이터플로깅",
                "강남구자원봉사센터에서는 탄소중립 실천을 위한 일환으로 강남구 플로깅 로드 프로젝트를 진행합니다. " +
                        "모바일 '이타서울' 웹앱을 활용한 비대면 플로깅 활동으로 활동을 통하여 기록된 정보는 강남구 플로깅 로드를 제작하는데에 활용됩니다.",
                "강남구",
                "2025.04.28 ~ 2025.07.27",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?progrmRegistNo=3280745"
            ),
            RecommendVolunteer(
                "저탄소생활실천 비대면[플로깅] 자원봉사 모집(※본인활동사진모습 3장 꼭 첨부,하루2시간까지/월2회인정)",
                "저탄소 생활실천「플로깅(Plogging)」자원봉사자 모집 안내. 첨부파일 예시 꼭 확인 후 활동.",
                "강북구",
                "2025.04.01 ~ 2025.06.30",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?progrmRegistNo=3269955"
            ),
            RecommendVolunteer(
                "[노원구자원봉사센터] 기후위기 대응 플로깅 자원봉사 (모집중)",
                "온라인 활동 인증 방식이며 자료 제출일 기준 실적 인정. (매월 1회 참여 가능)",
                "노원구",
                "2025.04.01 ~ 2025.06.30",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?progrmRegistNo=3258239"
            ),
            RecommendVolunteer(
                "서초구립주간이용센터 수플로(숲+플로깅) 프로그램 자원봉사",
                "성인 중증장애인의 의미있는 낮 활동 지원을 위한 자원봉사 모집.",
                "서초구",
                "2025.05.12 ~ 2025.08.11",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?progrmRegistNo=3279629"
            ),
            RecommendVolunteer(
                "★5월 구석구석 플로깅(지구를 살리는 작은 실천-비대면 환경정화)",
                "학생봉사활동 인정 여부는 학교장 확인 필요. 사전 상담 필수.",
                "이천시",
                "2025.05.01 ~ 2025.05.31",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?progrmRegistNo=3284381"
            ),
            RecommendVolunteer(
                "기흥호수공원 플로깅 및 신재생에너지 알아보기",
                "신재생에너지에 대해 알아보고, 플로깅을 통해 수생태계 보호.",
                "용인시",
                "2025.06.15",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?progrmRegistNo=3286629"
            ),
            RecommendVolunteer(
                "★꽃길만 걸을 고양_플로깅★",
                "산책하며 쓰레기 줍깅 활동. 사진 촬영 필수 (전/중/후). 타임스탬프 포함 제출 필요.",
                "고양시",
                "2025.04.01 ~ 2025.06.30",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?progrmRegistNo=3260318"
            ),
            RecommendVolunteer(
                "2025 우리같이 플로깅 활동",
                "신청한 장소에서 1시간 30분 이상 플로깅 활동 시 인정.",
                "계양구",
                "2025.05.01 ~ 2025.07.31",
                "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?progrmRegistNo=3277307"
            )
        )
    }
}

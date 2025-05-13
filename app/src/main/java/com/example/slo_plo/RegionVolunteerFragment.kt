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
        recommendVolunteerAdapter = RecommendVolunteerAdapter(filteredList) // 필터된 리스트 전달
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
            RecommendVolunteer("[강남구자원봉사센터-공감그린스토리] 같이 함께하는 강남구플로깅로드(오프라인)_쓰레기줍기 환경정화", "강남구자원봉사센터에서는 탄소중립 실천을 위한 일환으로 강남구 플로깅 로드 프로젝트를 진행합니다. " +
                    "강남구 플로깅 로드는 '이타서울'웹사이트을 활용한 모바일 플로깅(쓰레기줍기) 활동으로 기록된 정보는 강남구 플로깅 로드를 제작하는데에 활용됩니다. ", "강남구", "2025.05.09 ~ 2025.07.11", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3281268"),
            RecommendVolunteer("[강남구자원봉사센터]강남구플로깅로드_강남구 내 모바일 데이터플로깅", "강남구자원봉사센터에서는 탄소중립 실천을 위한 일환으로 강남구 플로깅 로드 프로젝트를 진행합니다. 강남구 플로깅 로드는 모바일 '이타서울' 웹앱을 활용한 비대면 플로깅 활동으로 활동을 통하여 기록된 정보는 강남구 플로깅 로드를 제작하는데에 활용됩니다. ", "강남구", "2025.04.28 ~ 2025.07.27", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3280745"),
            RecommendVolunteer("저탄소생활실천 비대면[플로깅] 자원봉사 모집(※본인활동사진모습 3장 꼭 첨부,하루2시간까지/월2회인정)", "저탄소 생활실천「플로깅(Plogging)」자원봉사자 모집 안내저탄소생활실천[플로깅]자원봉사 모집(※ 첨부파일 예시 꼭!!! 확인 후 활동)", "강북구", "2025.04.01 ~ 2025.06.30", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3269955"),
            RecommendVolunteer("[노원구자원봉사센터] 기후위기 대응 플로깅 자원봉사 (모집중)", "온라인 활동인증 방식이며, 신청하신 봉사 활동 날짜와 관계없이 실제 자료 제출이 완료된 날을 기준으로 실적이 입력됩니다. (매월 1회 참여가능)", "노원구", "2025.04.01 ~ 2025.06.30", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3258239"),
            RecommendVolunteer("서초구립주간이용센터 수플로(숲+플로깅) 프로그램 자원봉사", "저희 서초구립주간이용센터는 성인 중증장애인의 의미있는 낮 활동과 가족의 쉼을 지원하고자\n" +
                    "서초구청이 설립하고 사회복지법인 서울가톨릭사회복지회가 운영하고 있는\n" +
                    "중증장애인 주간이용시설입니다.\n" +
                    "저희 센터에서는 다음과 같이 자원봉사자를 모집합니다.", "서초구", "2025.05.12 ~ 2025.08.11", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3279629"),
            RecommendVolunteer("★5월 구석구석 플로깅(지구를 살리는 작은 실천-비대면 환경정화", "구석 구석 플로깅(우리들의 작은 실천 플로깅-비대면 환경정화)\n" +
                    "경기도교육청 학생봉사활동 운영계획에 따라 학생봉사활동의 최종 봉사시간\n" +
                    "인정여부는 학교장이 승인해야 합니다. 인정기관 및 인정활동 여부에 대해 반드시\n" +
                    "해당교에 사전 상담하세요.", "이천시", "2025.05.01 ~ 2025.05.31", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3284381"),
            RecommendVolunteer("기흥호수공원 플로깅 및 신재생에너지 알아보기", "태양열조리기 설치를 통해 신재생에너지에 대해 알아보고, 플로깅을 통해 하천의 수생태계를 보호한다.", "용인시", "2025.06.15", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3286629"),
            RecommendVolunteer("★꽃길만 걸을 고양_플로깅★", "우리 동네 공원 및 인도 등 산책하며 쓰레기 줍깅 활동\n" +
                    "           *** 활동 전 사진촬영->활동 중 사진촬영->활동 후 사진활영 (각각 한장 이상씩 사진찍기!)\n" +
                    "             ** 반드시 인물과 장소가 나오게 찍어주세요!\n" +
                    "              * 타임스탬프 어플로 사진찍어서 구글폼 링크를 통해 제출해주세요!", "고양시", "2025.04.01 ~ 2025.06.30", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3260318"),
            RecommendVolunteer("2025 우리같이 플로깅 활동", "1인 이상 신청한 장소에서 해당 시간에 플로깅(산책+쓰레기줍기) 활동\n" +
                    "            1시간30분 이상 활동 시  인정", "계양구", "2025.05.01 ~ 2025.07.31", "https://www.1365.go.kr/vols/1572247904127/partcptn/timeCptn.do?titleNm=%EC%83%81%EC%84%B8%EB%B3%B4%EA%B8%B0&type=show&progrmRegistNo=3277307")
        )
    }
}


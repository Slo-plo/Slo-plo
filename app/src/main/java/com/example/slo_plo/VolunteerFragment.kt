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

        /* 인스타 공유 버튼 로직
        binding.btnSharePage.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, InstaShareFragment()) // Fragment 교체
                .addToBackStack(null) // Back Stack에 추가
                .commit() // 트랜잭션 실행
        }
        */

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
            val regions = listOf("서울", "경기", "인천", "강원", "대전", "대구", "부산", "울산", "경상", "광주", "전라", "충청", "대전", "제주")
            val regionMap = mapOf(
                "서울" to listOf("강남구", "강동구", "강북구", "강서구", "관악구", "구로구", "금천구", "노원구",
                    "도봉구", "동작구", "마포구", "서대문구", "서초구", "성북구", "송파구",
                    "양천구", "영등포구", "은평구"),
                "경기" to listOf("가평군", "고양시", "과천시", "광명시", "광주시", "구리시", "군포시", "김포시",
                    "남양주시", "동두천시", "부천시", "성남시", "수원시", "시흥시", "안산시", "안성시",
                    "안양시", "양주시", "양평군", "여주시", "연천군", "오산시", "용인시", "의왕시",
                    "의정부시", "이천시", "파주시", "평택시", "포천시", "하남시", "화성시"),
                "인천" to listOf("강화군", "계양구", "남구", "남동구", "동구", "부평구", "서구", "연수구", "옹진군", "중구"),
                "강원" to listOf("강릉시", "고성군", "동해시", "삼척시", "속초시", "양구군", "양양군", "영월군",
                    "원주시", "인제군", "정선군", "철원군", "춘천시", "태백시", "평창군", "홍천군",
                    "화천군", "횡성군"),
                "대전" to listOf("대덕구", "동구", "서구", "유성구", "중구"),
                "대구" to listOf("남구", "달서구", "달성군", "동구", "북구", "서구", "수성구", "중구"),
                "부산" to listOf("강서구", "금정구", "기장군", "남구", "동구", "동래구", "부산진구", "북구",
                    "사상구", "사하구", "서구", "수영구", "연제구", "영도구", "중구", "해운대구"),
                "울산" to listOf("동구", "서구", "울주군", "유성구", "중구"),
                "경상" to listOf("거제시", "거창군", "고성군", "김해시", "남해군", "밀양시", "사천시", "산청군", "상주시",
                    "양산시", "진주시", "창녕군", "창원시", "청도군", "통영시", "하동군", "함안군",
                    "함양군", "합천군", "경산시", "경주시", "고령군", "구미시", "군위군", "김천시", "문경시", "봉화군",
                    "영덕군", "영양군", "영주시", "영천시", "예천군", "울릉군", "울진군", "의령군",
                    "의성군", "청송군", "칠곡군", "포항시"),
                "광주" to listOf("광산구", "남구", "동구", "북구", "서구"),
                "전라" to listOf("강진군", "고흥군", "곡성군", "광양시", "구례군", "담양군", "목포시", "무안군", "보성군",
                    "순창군", "순천시", "신안군", "여수시", "영광군", "영암군", "완도군", "완주군",
                    "임실군", "장성군", "장수군", "장흥군", "진도군", "진안군", "함평군", "해남군", "화순군",
                    "군산시", "김제시", "나주시", "남원시", "전주시", "정읍시", "익산시"),
                "충청" to listOf("계룡시", "공주시", "괴산군", "금산군", "논산시", "단양군", "당진시", "보령시", "보은군",
                    "부여군", "서산시", "서천군", "아산시", "영동군", "예산군", "옥천군", "음성군",
                    "제천시", "증평군", "진천군", "천안시", "청양군", "청주시", "충주시", "태안군", "홍성군"),
                "제주특별자치도" to listOf("제주", "서귀포")
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
                    val fullRegion = "$selectedSubRegion"

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.slo_plo

import android.graphics.Color
import android.os.Bundle
// import android.util.Log // 로그를 위해 추가 -> 삭제
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast // 오류 메시지 표시를 위해 추가
// import androidx.core.content.ContentProviderCompat.requireContext // requireContext는 Fragment에서 직접 사용 가능하므로 제거
import androidx.fragment.app.Fragment // Fragment import 유지
// import androidx.navigation.fragment.findNavController // RegionVolunteerFragment에서는 네비게이션 사용 안 하므로 제거
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
// BottomSheetLocationBinding import 추가
import com.example.slo_plo.databinding.BottomSheetLocationBinding
import com.example.slo_plo.databinding.FragmentRegionVolunteerBinding
import com.example.slo_plo.model.RecommendVolunteer // 데이터 클래스 임포트 확인

// Material Components Bottom Sheet 관련 임포트
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior

// Firebase Firestore 관련 임포트
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

class RegionVolunteerFragment : Fragment() {

    private var _binding: FragmentRegionVolunteerBinding? = null
    private val binding get() = _binding!!

    private var subAdapter: SubregionAdapter? = null
    private var selectedPosition: Int = -1

    private lateinit var recommendVolunteerAdapter: RecommendVolunteerAdapter

    // Firebase Firestore 인스턴스 선언
    private val db = FirebaseFirestore.getInstance()
    // 데이터가 저장된 컬렉션 이름
    private val volunteersCollection = db.collection("volunteer_lists") // 컬렉션 이름 확인 및 수정 필요

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegionVolunteerBinding.inflate(inflater, container, false)
        // onCreateView에서는 바인딩된 루트 뷰만 반환하고 나머지 초기화는 onViewCreated에서 수행합니다.
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 인자로 전달받은 지역명 가져오기 (VolunteerFragment에서 Bundle로 넘긴 "region" 키 사용)
        val initialRegion = arguments?.getString("region") ?: "지역명 오류" // <-- Bundle에서 초기 지역 값을 가져옵니다.
        binding.btnSelectRegion.text = initialRegion // 버튼 텍스트 업데이트 (UI 확인용)

        // 리사이클러뷰 설정 (초기에는 빈 데이터로 설정)
        setupRecyclerView()

        // Firebase에서 필터링된 데이터 로드 (초기 지역 값 사용)
        loadFilteredVolunteersFromFirebase(initialRegion)

        // TODO: 로딩 상태 표시 (예: ProgressBar) 초기 숨김/표시 설정은 setupRecyclerView나 loadFilteredVolunteersFromFirebase 시작 부분에서 합니다.


        // === 바텀시트 관련 코드 시작 (onViewCreated 안에서 처리) ===

        // binding.btnSelectRegion 버튼 클릭 시 지역 선택 바텀시트 표시
        binding.btnSelectRegion.setOnClickListener {
            // 바텀시트 다이얼로그 생성
            val dialog = BottomSheetDialog(requireContext())
            // 바텀시트 레이아웃 바인딩
            val sheetBinding = BottomSheetLocationBinding.inflate(layoutInflater)
            val dialogView = sheetBinding.root

            // 바텀시트 높이 설정
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val sheetHeight = (screenHeight * 0.75).toInt()

            dialog.setContentView(dialogView)

            // 바텀시트 내부 뷰 속성 조정 및 드래그 방지 설정
            val bottomSheet = dialog.delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED // 펼친 상태로 시작
                behavior.isHideable = false // 아래로 스와이프 시 완전히 숨겨지지 않도록
                behavior.skipCollapsed = true // 축소 상태 건너뛰기
                it.layoutParams.height = sheetHeight // 높이 설정
                it.requestLayout()

                // 드래그로 닫히지 않도록 상태 감시 콜백 추가
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(sheet: View, newState: Int) {
                        // 드래그 또는 축소 상태일 때 펼친 상태로 고정
                        if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    override fun onSlide(sheet: View, offset: Float) {
                        // 슬라이딩 중 필요한 동작이 있다면 여기에 추가
                    }
                })
            }

            // 지역 리스트 및 맵핑 (하드코딩 유지) - VolunteerFragment에서 복사한 데이터 그대로 사용
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
            val regionAdapter = RegionAdapter(requireContext(), regions)
            sheetBinding.listRegion.adapter = regionAdapter

            sheetBinding.listRegion.setOnItemClickListener { _, _, position, _ ->
                selectedRegion = regions[position]
                selectedPosition = position
                val subRegions = regionMap[selectedRegion] ?: emptyList()

                subAdapter = SubregionAdapter(requireContext(), subRegions)
                sheetBinding.listSubregion.adapter = subAdapter

                // 서브 지역 클릭 리스너는 한 번만 설정
                sheetBinding.listSubregion.setOnItemClickListener { _, _, position, _ ->
                    subAdapter?.selectedPosition = position
                    subAdapter?.notifyDataSetChanged()
                    selectedSubRegion = subAdapter?.getItem(position)
                }
            }

            // 서브 지역 선택 처리
            sheetBinding.listSubregion.setOnItemClickListener { _, view, position, _ ->
                // 선택된 아이템의 배경색을 어두운 색으로 변경
                view.setBackgroundResource(R.color.main_color)

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

            // 'X 창 닫기' 클릭 시 다이얼로그 닫기
            sheetBinding.tvRegionClose.setOnClickListener { dialog.dismiss() }

            // '지역 선택' 버튼 (바텀시트 안의 버튼) 클릭 시
            // RegionVolunteerFragment에서는 새로운 지역으로 데이터를 갱신합니다.
            sheetBinding.btnBsSelectRegion.setOnClickListener {
                if (selectedRegion != null && selectedSubRegion != null) {
                    val fullRegion: String = selectedSubRegion.toString() // 또는 필요한 형식으로 조합

                    // === Navigation Component로 이동하는 대신, 현재 Fragment의 데이터를 갱신합니다. ===

                    // 1. 화면에 표시된 지역명 버튼 텍스트를 업데이트합니다.
                    binding.btnSelectRegion.text = fullRegion

                    // 2. 새로운 지역에 맞춰 Firebase 데이터를 다시 로드합니다.
                    loadFilteredVolunteersFromFirebase(fullRegion)

                    // === 데이터 갱신 로직 끝 ===

                    dialog.dismiss() // 바텀시트 다이얼로그 닫기

                } else {
                    // 지역 선택 안 했을 때 토스트 메시지
                    Toast.makeText(requireContext(), "지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            // 바텀시트 다이얼로그 표시
            dialog.show()
        }

        // === 바텀시트 관련 코드 끝 ===

        // 나머지 초기화 및 데이터 로드는 onViewCreated 시작 부분에서 이미 호출되었습니다.

    }


    //리사이클러뷰 초기 설정 함수
    private fun setupRecyclerView() {
        recommendVolunteerAdapter = RecommendVolunteerAdapter(emptyList()) // 어댑터를 초기화할 때 빈 리스트를 넘겨줍니다.

        // 여기서 binding.recyclerViewResionVolunteer 를 사용합니다.
        binding.recyclerViewResionVolunteer.apply { // <-- 올바른 RecyclerView ID 사용
            layoutManager = LinearLayoutManager(requireContext()) // <-- layoutManager는 여기서 사용
            adapter = recommendVolunteerAdapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            )
        }

        // 로딩 상태 UI 초기 설정
        binding.recyclerViewResionVolunteer.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE
        // binding.progressBar.visibility = View.VISIBLE // ProgressBar가 있다면 로딩 시작 시 표시
    }


    //Firebase Firestore에서 지역별 봉사활동 데이터를 가져오는 함수
    private fun loadFilteredVolunteersFromFirebase(region: String) {
        // 데이터 로드 시작 시 로딩 표시
        // binding.progressBar.visibility = View.VISIBLE // ProgressBar가 있다면
        binding.recyclerViewResionVolunteer.visibility = View.GONE // 리사이클러뷰 숨김
        binding.tvEmptyMessage.visibility = View.GONE // Empty Message 숨김

        volunteersCollection
            // 'location' 필드가 전달받은 region 값과 같은 문서를 필터링
            .whereEqualTo("location", region)
            .get() // 필터링된 문서를 한 번 가져옴 (실시간 업데이트는 addSnapshotListener 사용)
            .addOnSuccessListener { querySnapshot: QuerySnapshot? ->
                // 데이터 로드 성공 시 로딩 숨김
                // binding.progressBar.visibility = View.GONE // ProgressBar가 있다면

                val volunteers = mutableListOf<RecommendVolunteer>()
                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        // toObject 변환 시 오류 발생 가능성 대비
                        try {
                            val volunteer = document.toObject(RecommendVolunteer::class.java)
                            if (volunteer != null) {
                                volunteers.add(volunteer)
                            } else {
                                // Log.w("RegionVolunteer", "Failed to convert document ${document.id} to RecommendVolunteer (object is null).") // 로그 삭제
                            }
                        } catch (e: Exception) {
                            // Log.e("RegionVolunteer", "Error converting document ${document.id} to RecommendVolunteer", e) // 로그 삭제
                            // 필요하다면 사용자에게 오류 알림 추가
                        }
                    }
                }

                // 가져온 데이터로 리사이클러뷰 어댑터 업데이트
                recommendVolunteerAdapter.updateData(volunteers)

                // 데이터 로드 후 Empty View 처리
                if (volunteers.isEmpty()) {
                    binding.recyclerViewResionVolunteer.visibility = View.GONE
                    binding.tvEmptyMessage.visibility = View.VISIBLE // Empty Message 표시
                    binding.tvEmptyMessage.text = "해당 지역의 봉사활동이 아직 없습니다." // 기본 메시지 또는 지역에 맞는 메시지 설정
                } else {
                    binding.recyclerViewResionVolunteer.visibility = View.VISIBLE
                    binding.tvEmptyMessage.visibility = View.GONE // Empty Message 숨김
                }

                // Log.d("RegionVolunteer", "Successfully loaded ${volunteers.size} filtered volunteers for $region.") // 로그 삭제

            }
            .addOnFailureListener { exception: Exception ->
                // 데이터 로드 실패
                // binding.progressBar.visibility = View.GONE // ProgressBar가 있다면

                // Log.w("RegionVolunteer", "Error getting filtered documents for $region: ", exception) // 로그 삭제
                // 로드 실패 시 사용자에게 알림
                Toast.makeText(requireContext(), "데이터 로드 실패: ${exception.message}", Toast.LENGTH_SHORT).show()

                // 데이터 로드 실패 시 Empty View 또는 오류 메시지 표시
                binding.recyclerViewResionVolunteer.visibility = View.GONE
                binding.tvEmptyMessage.visibility = View.VISIBLE
                binding.tvEmptyMessage.text = "데이터를 불러오는데 실패했습니다.\n(${exception.message})" // 실패 메시지로 변경
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // TODO: 만약 addSnapshotListener를 사용했다면 여기서 리스너 제거 코드가 필수입니다.
    }

    // Fragment가 View를 가지고 있지 않을 때 Firebase 작업 등을 취소해야 할 수 있습니다.
    // 필요하다면 onStop 또는 onPause에서 Firebase 리스너 해제, onDestroy에서 리스너 제거 등 생명주기 관리를 합니다.
}
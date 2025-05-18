package com.example.slo_plo

import android.graphics.Color
import android.os.Bundle
// import android.util.Log // Logcat 출력을 위해 추가 -> 요청에 따라 삭제
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.slo_plo.databinding.FragmentVolunteerBinding
import com.example.slo_plo.databinding.BottomSheetLocationBinding
import com.example.slo_plo.model.RecommendVolunteer // 데이터 클래스 임포트 확인
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.navigation.fragment.findNavController

// Firebase Firestore 관련 임포트 추가
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

class VolunteerFragment : Fragment() {

    private var _binding: FragmentVolunteerBinding? = null
    private val binding get() = _binding!!

    private lateinit var recommendVolunteerAdapter: RecommendVolunteerAdapter

    // Firebase Firestore 인스턴스 선언 (늦은 초기화 또는 lazy 초기화)
    private val db = FirebaseFirestore.getInstance()
    // 데이터가 저장된 컬렉션 이름 (3단계에서 설계한 이름 사용)
    private val volunteersCollection = db.collection("volunteer_lists") // 컬렉션 이름 확인 및 수정 필요

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVolunteerBinding.inflate(inflater, container, false)
        val view = binding.root

        // 'btnFindLocation' 버튼 클릭 리스너 설정 (기존 로직 유지)
        binding.btnFindLocation.setOnClickListener {
            // ... (바텀시트 관련 기존 코드 그대로 유지) ...

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

            // 지역 리스트 정의 및 맵핑 (기존 데이터 유지)
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
            sheetBinding.btnBsSelectRegion.setOnClickListener {
                if (selectedRegion != null && selectedSubRegion != null) {
                    // RegionVolunteerFragment로 전달할 지역명 준비
                    val fullRegion = selectedSubRegion // 또는 필요한 형식으로 조합

                    // RegionVolunteerFragment로 전달할 데이터를 Bundle에 담음
                    val bundle = Bundle().apply {
                        putString("region", fullRegion) // 키는 RegionVolunteerFragment에서 가져올 때 사용할 이름("region")과 일치해야 함
                    }

                    // Navigation Component의 findNavController() 사용
                    val navController = findNavController()

                    // 네비게이션 그래프에 정의된 액션 ID를 사용하여 이동
                    // R.id.action_volunteerFragment_to_regionVolunteerFragment 는 navigation.xml에 추가한 액션의 ID
                    // bundle에는 전달할 인자(region)가 담겨 있음
                    navController.navigate(R.id.action_volunteerFragment_to_regionVolunteerFragment, bundle)

                    dialog.dismiss() // 바텀시트 다이얼로그 닫기 (기존 코드 유지)

                } else {
                    // 지역 선택 안 했을 때 토스트 메시지 (기존 코드 유지)
                    Toast.makeText(requireContext(), "지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            // 다이얼로그 표시
            dialog.show()
        }


        // 리사이클러뷰 설정
        // 어댑터를 초기화할 때 빈 리스트를 넘겨줌
        recommendVolunteerAdapter = RecommendVolunteerAdapter(emptyList())
        binding.recyclerViewRecommendVolunteer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendVolunteerAdapter

            val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }

        // TODO: 로딩 상태 표시 (예: ProgressBar)를 여기에 추가할 수 있습니다.
        // binding.progressBar.visibility = View.VISIBLE

        // Firebase에서 데이터를 가져오는 함수 호출
        loadVolunteersFromFirebase()


        return view
    }

    // Firebase Firestore에서 봉사활동 데이터를 가져오는 함수
    private fun loadVolunteersFromFirebase() {
        volunteersCollection
            .get() // get() 메서드로 컬렉션의 모든 문서를 한 번 가져옵니다.
            .addOnSuccessListener { querySnapshot: QuerySnapshot? ->
                val volunteers = mutableListOf<RecommendVolunteer>()
                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        try {
                            val volunteer = document.toObject(RecommendVolunteer::class.java)
                            if (volunteer != null) {
                                // Log.d("LinkConvertDebug", "Document ID: ${document.id}, Converted link: '${volunteer.link}'") // 로그 삭제
                                volunteers.add(volunteer)
                            } else {
                                // Log.w("LinkConvertDebug", "Document ${document.id} converted to null.") // 로그 삭제
                            }
                        } catch (e: Exception) {
                            // Log.e("Firebase", "Error converting document to RecommendVolunteer: ${document.id}", e) // 로그 삭제
                            // 데이터 변환 오류 시 로그 출력 (Firestore 필드 이름/타입 불일치 가능성)
                        }
                    }
                }

                // 가져온 데이터로 리사이클러뷰 어댑터 업데이트
                recommendVolunteerAdapter.updateData(volunteers) // 어댑터 업데이트 함수 호출

                // TODO: 로딩 상태 표시를 숨깁니다.
                // binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception: Exception ->
                Toast.makeText(requireContext(), "데이터 로드 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                // TODO: 로딩 상태 표시를 숨깁니다.
                // binding.progressBar.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // TODO: 만약 addSnapshotListener를 사용했다면 여기서 리스너를 제거해야 메모리 누수를 방지할 수 있습니다.
    }
}
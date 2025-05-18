package com.example.slo_plo

import android.graphics.Color
import android.os.Bundle
import android.util.Log // Logcat 출력을 위해 추가
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

        /* 인스타 공유 버튼 로직
        binding.btnSharePage.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, InstaShareFragment()) // Fragment 교체
                .addToBackStack(null) // Back Stack에 추가
                .commit() // 트랜잭션 실행
        }
        */

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
            sheetBinding.btnSelectRegion.setOnClickListener {
                if (selectedRegion != null && selectedSubRegion != null) {
                    val fullRegion = "$selectedSubRegion"

                    // TODO: RegionVolunteerFragment에서도 Firebase 데이터를 해당 지역으로 필터링하여 가져와야 합니다.
                    // 이 부분의 로직도 추후 Firebase 연동에 맞게 수정이 필요합니다.
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
        // 어댑터를 초기화할 때 빈 리스트를 넘겨줍니다.
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

    // 하드코딩된 데이터를 가져오는 함수는 이제 필요 없으므로 제거합니다.
    /*
    private fun getVolunteerList(): List<RecommendVolunteer> {
        // ... 기존 하드코딩 데이터 ...
        return listOf(...)
    }
    */


    // Firebase Firestore에서 봉사활동 데이터를 가져오는 함수
    private fun loadVolunteersFromFirebase() {
        volunteersCollection
            .get() // get() 메서드로 컬렉션의 모든 문서를 한 번 가져옵니다.
            .addOnSuccessListener { querySnapshot: QuerySnapshot? ->
                // 데이터 로드 성공
                val volunteers = mutableListOf<RecommendVolunteer>()
                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        // 각 DocumentSnapshot을 RecommendVolunteer 객체로 변환
                        // RecommendVolunteer 데이터 클래스의 필드 이름과 Firestore 문서의 필드 이름이 정확히 일치해야 합니다.
                        try {
                            val volunteer = document.toObject(RecommendVolunteer::class.java)
                            if (volunteer != null) {
                                volunteers.add(volunteer)
                            }
                        } catch (e: Exception) {
                            Log.e("Firebase", "Error converting document to RecommendVolunteer: ${document.id}", e)
                            // 데이터 변환 오류 시 로그 출력 (Firestore 필드 이름/타입 불일치 가능성)
                        }
                    }
                }

                // 가져온 데이터로 리사이클러뷰 어댑터 업데이트
                // RecommendVolunteerAdapter에 updateData(newList: List<RecommendVolunteer>) 함수가 있다고 가정합니다.
                // 만약 없다면 어댑터에 이 함수를 추가해야 합니다.
                recommendVolunteerAdapter.updateData(volunteers) // 어댑터 업데이트 함수 호출

                // TODO: 로딩 상태 표시를 숨깁니다.
                // binding.progressBar.visibility = View.GONE

                Log.d("Firebase", "Successfully loaded ${volunteers.size} volunteers from Firebase.")

            }
            .addOnFailureListener { exception: Exception ->
                // 데이터 로드 실패
                Log.w("Firebase", "Error getting documents from Firebase: ", exception)
                // TODO: 로드 실패 시 사용자에게 알림 (Toast 등) 또는 오류 상태 표시
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

    // RecommendVolunteerAdapter 클래스에 데이터를 업데이트하는 함수가 필요합니다.
    // 예를 들어, RecommendVolunteerAdapter 안에 다음과 같은 함수를 추가합니다:
    /*
    class RecommendVolunteerAdapter(...) : RecyclerView.Adapter<...>() {
        private var volunteerList: List<RecommendVolunteer> = emptyList()

        constructor(initialList: List<RecommendVolunteer>) {
            volunteerList = initialList
        }

        // 이 함수를 추가하여 외부에서 데이터를 업데이트할 수 있도록 합니다.
        fun updateData(newList: List<RecommendVolunteer>) {
            volunteerList = newList
            notifyDataSetChanged() // 데이터가 변경되었음을 어댑터에 알려 리사이클러뷰를 갱신합니다.
            // TODO: 데이터 변경 효율을 위해 DiffUtil 사용을 고려해볼 수 있습니다.
        }

        // getItemCount, onCreateViewHolder, onBindViewHolder 등 기존 코드는 volunteerList를 사용하도록 유지
        override fun getItemCount(): Int = volunteerList.size

        // ... onCreateViewHolder, onBindViewHolder ...
    }
    */
}
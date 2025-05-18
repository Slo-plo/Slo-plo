package com.example.slo_plo

import android.os.Bundle
import android.util.Log // 로그를 위해 추가
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // 오류 메시지 표시를 위해 추가
import androidx.fragment.app.Fragment // Fragment import 유지
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slo_plo.databinding.FragmentRegionVolunteerBinding
import com.example.slo_plo.model.RecommendVolunteer // 데이터 클래스 임포트 확인

// Firebase Firestore 관련 임포트 추가
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

class RegionVolunteerFragment : Fragment() {

    private var _binding: FragmentRegionVolunteerBinding? = null
    private val binding get() = _binding!!

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 인자로 전달받은 지역명 가져오기 (VolunteerFragment에서 Bundle로 넘긴 "region" 키 사용)
        val region = arguments?.getString("region") ?: "지역명 오류" // <-- Bundle에서 값을 가져옵니다.
        binding.btnSelectRegion.text = region // 버튼 텍스트 업데이트 (UI 확인용)

        // 리사이클러뷰 설정 (초기에는 빈 데이터로 설정)
        setupRecyclerView()

        // Firebase에서 필터링된 데이터 로드
        loadFilteredVolunteersFromFirebase(region)

        // TODO: 로딩 상태 표시 (예: ProgressBar) 초기 숨김/표시 설정은 setupRecyclerView나 loadFilteredVolunteersFromFirebase 시작 부분에서 합니다.
    }


    //리사이클러뷰 초기 설정 함수
    private fun setupRecyclerView() {
        recommendVolunteerAdapter = RecommendVolunteerAdapter(emptyList()) // 어댑터를 초기화할 때 빈 리스트를 넘겨줍니다.

        binding.recyclerViewResionVolunteer.apply {
            layoutManager = LinearLayoutManager(requireContext())
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
        binding.recyclerViewResionVolunteer.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.GONE

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
                                Log.w("RegionVolunteer", "Failed to convert document ${document.id} to RecommendVolunteer (object is null).")
                            }
                        } catch (e: Exception) {
                            // 데이터 구조 불일치 등으로 변환 실패 시
                            Log.e("RegionVolunteer", "Error converting document ${document.id} to RecommendVolunteer", e)
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

                Log.d("RegionVolunteer", "Successfully loaded ${volunteers.size} filtered volunteers for $region.")

            }
            .addOnFailureListener { exception: Exception ->
                // 데이터 로드 실패
                // binding.progressBar.visibility = View.GONE // ProgressBar가 있다면

                Log.w("RegionVolunteer", "Error getting filtered documents for $region: ", exception)
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
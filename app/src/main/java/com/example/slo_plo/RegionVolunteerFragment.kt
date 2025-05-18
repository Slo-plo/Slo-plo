package com.example.slo_plo

import android.os.Bundle
import android.util.Log // 로그를 위해 추가
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // 오류 메시지 표시를 위해 추가
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
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
    // 데이터가 저장된 컬렉션 이름 (스크린샷에서 확인된 이름 사용)
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

        // 뒤로가기 버튼 처리 (기존 로직 유지)
        handleBackPressed()

        // 인자로 전달받은 지역명 가져오기
        val region = arguments?.getString("region") ?: "지역명 오류"
        binding.btnSelectRegion.text = region // 버튼 텍스트 업데이트

        // 리사이클러뷰 설정 (초기에는 빈 데이터로 설정)
        setupRecyclerView()

        // Firebase에서 필터링된 데이터 로드
        loadFilteredVolunteersFromFirebase(region)

        // TODO: 로딩 상태 표시 (예: ProgressBar)를 여기에 추가할 수 있습니다.
        // binding.progressBar.visibility = View.VISIBLE
    }

    private fun handleBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 이전 VolunteerFragment로 돌아가기
                parentFragmentManager.popBackStack() // Back Stack에서 Fragment를 제거하여 이전 화면으로 돌아감
                // 또는 replace를 사용하여 명시적으로 VolunteerFragment로 이동
                /*
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, VolunteerFragment())
                    .commit()
                 */
            }
        })
    }

    // 리사이클러뷰 초기 설정 함수
    private fun setupRecyclerView() {
        // 어댑터를 초기화할 때 빈 리스트를 넘겨줍니다.
        recommendVolunteerAdapter = RecommendVolunteerAdapter(emptyList())

        binding.recyclerViewResionVolunteer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendVolunteerAdapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            )
        }

        // 초기에는 Empty Message 숨김
        binding.tvEmptyMessage.visibility = View.GONE
    }


    // Firebase Firestore에서 지역별 봉사활동 데이터를 가져오는 함수
    private fun loadFilteredVolunteersFromFirebase(region: String) {
        volunteersCollection
            // 'location' 필드가 전달받은 region 값과 같은 문서를 필터링합니다.
            .whereEqualTo("location", region)
            .get() // 필터링된 문서를 한 번 가져옵니다.
            .addOnSuccessListener { querySnapshot: QuerySnapshot? ->
                // 데이터 로드 성공
                val volunteers = mutableListOf<RecommendVolunteer>()
                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        // 각 DocumentSnapshot을 RecommendVolunteer 객체로 변환
                        try {
                            val volunteer = document.toObject(RecommendVolunteer::class.java)
                            if (volunteer != null) {
                                volunteers.add(volunteer)
                            }
                        } catch (e: Exception) {
                            Log.e("Firebase", "Error converting document to RecommendVolunteer: ${document.id}", e)
                            // 데이터 변환 오류 시 로그 출력 (Firestore 필드 이름/타입 불일치 가능성)
                            // RecommendVolunteer 데이터 클래스에 기본값이 설정되었는지 다시 확인하세요.
                        }
                    }
                }

                // 가져온 데이터로 리사이클러뷰 어댑터 업데이트
                recommendVolunteerAdapter.updateData(volunteers) // 어댑터 업데이트 함수 호출

                // 데이터 로드 후 Empty View 처리
                if (volunteers.isEmpty()) {
                    binding.recyclerViewResionVolunteer.visibility = View.GONE
                    binding.tvEmptyMessage.visibility = View.VISIBLE // "아직 등록된 봉사활동이 없어요" 텍스트뷰
                } else {
                    binding.recyclerViewResionVolunteer.visibility = View.VISIBLE
                    binding.tvEmptyMessage.visibility = View.GONE
                }


                // TODO: 로딩 상태 표시를 숨깁니다.
                // binding.progressBar.visibility = View.GONE

                Log.d("Firebase", "Successfully loaded ${volunteers.size} filtered volunteers for $region.")

            }
            .addOnFailureListener { exception: Exception ->
                // 데이터 로드 실패
                Log.w("Firebase", "Error getting filtered documents from Firebase: ", exception)
                // TODO: 로드 실패 시 사용자에게 알림 (Toast 등) 또는 오류 상태 표시
                Toast.makeText(requireContext(), "${region} 데이터 로드 실패: ${exception.message}", Toast.LENGTH_SHORT).show()

                // 데이터 로드 실패 시 Empty View 표시 (선택 사항)
                binding.recyclerViewResionVolunteer.visibility = View.GONE
                binding.tvEmptyMessage.visibility = View.VISIBLE // 오류 메시지 대신 empty 메시지를 보여주거나, 별도 오류 메시지 텍스트뷰를 사용할 수 있습니다.


                // TODO: 로딩 상태 표시를 숨깁니다.
                // binding.progressBar.visibility = View.GONE
            }
    }

    // 하드코딩된 데이터를 가져오는 함수는 이제 필요 없으므로 제거합니다.
    /*
    private fun getVolunteerList(): List<RecommendVolunteer> {
        return listOf(...) // 하드코딩된 데이터
    }
    */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // TODO: 만약 실시간 업데이트를 위해 addSnapshotListener를 사용했다면 여기서 리스너를 제거해야 합니다.
    }
}
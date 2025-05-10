import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.slo_plo.databinding.FragmentVolunteerBinding
import com.example.slo_plo.databinding.BottomSheetLocationBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.ArrayAdapter
import com.example.slo_plo.RecommendVolunteerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.example.slo_plo.RecommendVolunteer


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

        binding.btnFindLocation.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val sheetBinding = BottomSheetLocationBinding.inflate(layoutInflater)
            val dialogView = sheetBinding.root

            // 화면 높이를 가져오기
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val sheetHeight = (screenHeight * 0.75).toInt()

            dialog.setContentView(dialogView)

            // BottomSheetBehavior 커스터마이징
            val bottomSheet = dialog.delegate.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isHideable = false
                behavior.skipCollapsed = true
                it.layoutParams.height = sheetHeight
                it.requestLayout()

                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(sheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(sheet: View, offset: Float) {}
                })
            }

            // 리스트 연결
            val regions = listOf("서울", "경기", "인천", "강원", "대전/충청", "대구", "부산/울산", "경상", "광주/전라")
            val regionMap = mapOf(
                "서울" to listOf("강남", "강변", "건대입구", "구로", "노원구", "대학로", "동대문", "동촌", "명동", "미아", "버전"),
                "경기" to listOf("수원", "성남", "용인")
                // 다른 지역에 대한 리스트 추가
            )

            val regionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, regions)
            sheetBinding.listRegion.adapter = regionAdapter

            sheetBinding.listRegion.setOnItemClickListener { _, _, position, _ ->
                val selected = regions[position]
                val subRegions = regionMap[selected] ?: emptyList()
                val subAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, subRegions)
                sheetBinding.listSubregion.adapter = subAdapter
            }

            sheetBinding.tvRegionClose.setOnClickListener { dialog.dismiss() }
            sheetBinding.btnSelectRegion.setOnClickListener {
                // 선택된 지역 처리 로직
                dialog.dismiss()
            }

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
            RecommendVolunteer("길거리 청소 봉사", "도심 거리 청소를 함께해요!", "노원구", "5월 12일"),
            RecommendVolunteer("노인복지센터 지원", "말벗이 되어 드리고 식사 보조도 해요.", "강남구", "6월 12일"),
            RecommendVolunteer("지역 아동센터 미술 수업", "아이들과 창의력 넘치는 시간을 보내요.", "노원구", "5월 12일"),
            RecommendVolunteer("동물 보호소 봉사", "유기동물들과 산책하며 돌봐줘요.", "노원구", "5월 12일"),
            RecommendVolunteer("환경 보호 캠페인", "자연을 지키는 활동에 참여해요.", "노원구", "5월 12일"),
            RecommendVolunteer("이웃 지원 봉사", "독거노인 분들에게 필요한 물품을 전달해요.", "노원구", "5월 12일"),
            RecommendVolunteer("청소년 멘토링", "청소년들에게 진로 상담을 해줘요.", "노원구", "5월 12일"),
            RecommendVolunteer("해변 청소 봉사", "해변에서 쓰레기를 청소해요.", "노원구", "5월 12일")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

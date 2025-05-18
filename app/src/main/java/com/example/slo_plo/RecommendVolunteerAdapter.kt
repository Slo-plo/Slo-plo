package com.example.slo_plo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.slo_plo.databinding.ItemRecomendVolunteerBinding
import com.example.slo_plo.model.RecommendVolunteer // 데이터 클래스 임포트 확인

class RecommendVolunteerAdapter(initialList: List<RecommendVolunteer>) :
    RecyclerView.Adapter<RecommendVolunteerAdapter.VolunteerViewHolder>() {

    // 데이터를 저장할 리스트를 val에서 var로 변경하고 초기 데이터를 받습니다.
    private var recommendVolunteerList: List<RecommendVolunteer> = initialList

    // 데이터를 새로 받아와서 리사이클러뷰를 업데이트하는 함수 추가
    fun updateData(newList: List<RecommendVolunteer>) {
        recommendVolunteerList = newList // 새로운 리스트로 데이터 갱신
        notifyDataSetChanged() // 데이터가 변경되었음을 어댑터에 알려 리사이클러뷰를 새로고침 (간단한 방식)
        // 참고: 대량의 데이터 변경 시 성능 향상을 위해 DiffUtil 사용을 고려해볼 수 있습니다.
    }

    inner class VolunteerViewHolder(private val binding: ItemRecomendVolunteerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recommendVolunteer: RecommendVolunteer) {
            binding.tvRecommendTitle.text = recommendVolunteer.title
            binding.tvRecommendDescription.text = recommendVolunteer.description
            binding.tvRecommendLocation.text = recommendVolunteer.location
            binding.tvRecommendDate.text = recommendVolunteer.date

            // 클릭 이벤트 처리 (기존 코드 그대로 유지)
            binding.root.setOnClickListener {
                // 외부 링크로 이동할지 여부를 묻는 메시지 박스
                showCustomMessageBox(it.context, recommendVolunteer.link)
            }
        }
    }

    // showCustomMessageBox 함수 (기존 코드 그대로 유지)
    private fun showCustomMessageBox(context: Context, link: String) {
        // dialog_default.xml 레이아웃을 인플레이트
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_default, null)

        // 다이얼로그 객체 생성
        val dialog = android.app.Dialog(context)
        dialog.setContentView(dialogView)

        // 다이얼로그 내용 설정
        val titleView: TextView = dialogView.findViewById(R.id.tv_default_title)
        val messageView: TextView = dialogView.findViewById(R.id.tv_default_content)
        val confirmButton: Button = dialogView.findViewById(R.id.btn_default_yes)
        val cancelButton: Button = dialogView.findViewById(R.id.btn_default_no)

        titleView.text = "외부로 이동"
        messageView.text = "이 봉사활동의 링크로 이동하시겠습니까?"

        // 네 버튼 클릭 시 링크로 이동
        confirmButton.setOnClickListener {
            // 링크가 비어있거나 null인지, 또는 유효한 URL 형식인지 간단히 확인
            if (link.isNullOrEmpty() || !android.util.Patterns.WEB_URL.matcher(link).matches()) {
                Toast.makeText(context, "유효한 링크 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                try {
                    // 유효한 경우에만 Intent 실행
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                    dialog.dismiss()
                } catch (e: ActivityNotFoundException) {
                    // 혹시 Intent를 처리할 앱이 정말 없을 경우를 대비 (드문 경우)
                    Toast.makeText(context, "링크를 열 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    Log.e("Adapter", "Activity not found for URL: $link", e)
                } catch (e: Exception) {
                    // 그 외 다른 오류 처리
                    Toast.makeText(context, "링크 열기 중 오류 발생.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    Log.e("Adapter", "Error opening URL: $link", e)
                }
            }
        }

        // 아니오 버튼 클릭 시 다이얼로그 닫기
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // 다이얼로그 표시
        dialog.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolunteerViewHolder {
        val binding = ItemRecomendVolunteerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VolunteerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VolunteerViewHolder, position: Int) {
        // var로 변경된 recommendVolunteerList를 사용
        holder.bind(recommendVolunteerList[position])
    }

    override fun getItemCount() = recommendVolunteerList.size // var로 변경된 recommendVolunteerList를 사용
}
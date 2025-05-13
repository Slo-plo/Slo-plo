package com.example.slo_plo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slo_plo.databinding.ItemRecomendVolunteerBinding

class RecommendVolunteerAdapter(private val recommendVolunteerList: List<RecommendVolunteer>) :
    RecyclerView.Adapter<RecommendVolunteerAdapter.VolunteerViewHolder>() {

    inner class VolunteerViewHolder(private val binding: ItemRecomendVolunteerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recommendVolunteer: RecommendVolunteer) {
            binding.tvRecommendTitle.text = recommendVolunteer.title
            binding.tvRecommendDescription.text = recommendVolunteer.description
            binding.tvRecommendLocation.text = recommendVolunteer.location
            binding.tvRecommendDate.text = recommendVolunteer.date

            // 클릭 이벤트 처리
            binding.root.setOnClickListener {
                // 외부 링크로 이동할지 여부를 묻는 메시지 박스
                showCustomMessageBox(it.context, recommendVolunteer.link)
            }
        }
    }

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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            context.startActivity(intent)
            dialog.dismiss()
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
        holder.bind(recommendVolunteerList[position])
    }

    override fun getItemCount() = recommendVolunteerList.size
}

package com.example.slo_plo

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
                val builder = AlertDialog.Builder(it.context)
                builder.setMessage("이 봉사활동의 외부 링크로 이동하시겠습니까?")
                    .setPositiveButton("확인") { dialog, _ ->
                        // 확인 버튼 클릭 시 링크로 이동
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recommendVolunteer.link))
                        it.context.startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton("취소") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
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

package com.example.slo_plo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.slo_plo.databinding.ItemRecomendVolunteerBinding

class RecomendVolunteerAdapter(private val recomendVolunteerList: List<RecomendVolunteer>) :
    RecyclerView.Adapter<RecomendVolunteerAdapter.VolunteerViewHolder>() {

    inner class VolunteerViewHolder(private val binding: ItemRecomendVolunteerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recomendVolunteer: RecomendVolunteer) {
            binding.tvTitle.text = recomendVolunteer.title
            binding.tvDescription.text = recomendVolunteer.description
            // 아이템의 배경색을 흰색으로 설정
            binding.root.setBackgroundColor(itemView.context.getColor(R.color.white))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolunteerViewHolder {
        val binding = ItemRecomendVolunteerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VolunteerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VolunteerViewHolder, position: Int) {
        holder.bind(recomendVolunteerList[position])
    }

    override fun getItemCount() = recomendVolunteerList.size
}

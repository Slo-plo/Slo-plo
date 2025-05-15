package com.example.slo_plo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.slo_plo.databinding.ItemLogSummaryBinding
import com.example.slo_plo.model.LogRecord

class LogSummaryPagerAdapter(
    private val logs: List<LogRecord>,
    private val onDetailClick: (LogRecord) -> Unit
) : RecyclerView.Adapter<LogSummaryPagerAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: ItemLogSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: LogRecord) {
            binding.summaryDate.text = " ${log.dateId}"
            binding.summaryTitle.text = log.title
            binding.summaryTime.text = " ${log.time}분"
            binding.summaryDistance.text = " ${log.distance} m"
            binding.summaryAddress.text = " ${log.startAddress}"
            binding.summaryTrash.text = " 수거한 쓰레기: ${log.trashCount}개"

            //️ 상세보기 버튼 클릭 처리
            binding.buttonDetail.setOnClickListener {
                onDetailClick(log)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int = logs.size
}


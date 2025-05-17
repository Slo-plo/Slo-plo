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
            binding.summaryTitle.text = log.title
            binding.summaryContent.text = log.body
            binding.summaryTime.text = " ${log.time}분"
            binding.summaryDistance.text = " ${formatDistance(log.distance)}"
            // binding.summaryAddress.text = " ${log.startAddress}"
            binding.summaryTrash.text = "${log.trashCount}개"

            binding.layoutLogSummary.setOnClickListener {
                onDetailClick(log)
            }
        }

    }

    private fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format("%.1f km", meters / 1000)
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


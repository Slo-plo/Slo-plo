package com.example.slo_plo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.slo_plo.databinding.ItemLogListBinding
import com.example.slo_plo.model.LogRecord

class LogListAdapter(
    private val logList: List<LogRecord>,
    private val onDetailClick: (LogRecord) -> Unit
) : RecyclerView.Adapter<LogListAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: ItemLogListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val record = logList[position]
        val b = holder.binding

        b.summaryDate.text = " ${record.dateId}"
        b.summaryTime.text = " ${record.time}분"
        b.summaryDistance.text = " ${record.distance} m"
        b.summaryTitle.text = record.title
        b.summaryTrash.text = " ${record.trashCount}개"
        b.summaryContent.text = record.body

        b.root.setOnClickListener {
            onDetailClick(record)
        }
    }

    override fun getItemCount(): Int = logList.size
}

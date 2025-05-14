package com.example.slo_plo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slo_plo.R
import com.example.slo_plo.model.LogRecord

class LogListAdapter(
    private val logList: List<LogRecord>,
    private val onDetailClick: (LogRecord) -> Unit
) : RecyclerView.Adapter<LogListAdapter.LogViewHolder>() {

    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val summaryDate: TextView = itemView.findViewById(R.id.summaryDate)
        val summaryTime: TextView = itemView.findViewById(R.id.summaryTime)
        val summaryDistance: TextView = itemView.findViewById(R.id.summaryDistance)
        val summaryTitle: TextView = itemView.findViewById(R.id.summaryTitle)
        val summaryAddress: TextView = itemView.findViewById(R.id.summaryAddress)
        val summaryTrash: TextView = itemView.findViewById(R.id.summaryTrash)
        val summaryContent: TextView = itemView.findViewById(R.id.summaryContent)
        val buttonDetail: ImageButton = itemView.findViewById(R.id.buttonDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_list, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val record = logList[position]

        holder.summaryDate.text = "🗓️ ${record.dateId}"
        holder.summaryTime.text = " ${record.time}"
        holder.summaryDistance.text = " ${record.distance}"
        holder.summaryTitle.text = record.title
        holder.summaryAddress.text = "📍 ${record.startAddress}"
        holder.summaryTrash.text = "수거한 쓰레기: ${record.trashCount}개"
        holder.summaryContent.text =
            record.body.take(50) + if (record.body.length > 50) "..." else ""

        holder.buttonDetail.setOnClickListener {
            onDetailClick(record)
        }
    }

    override fun getItemCount(): Int = logList.size
}

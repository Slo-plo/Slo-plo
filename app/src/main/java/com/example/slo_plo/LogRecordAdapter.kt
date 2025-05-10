//package com.example.slo_plo.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.slo_plo.databinding.ItemLogRecordBinding
//import com.example.slo_plo.model.LogRecord
//
//class LogRecordAdapter(private val records: List<LogRecord>) :
//    RecyclerView.Adapter<LogRecordAdapter.RecordViewHolder>() {
//
//    inner class RecordViewHolder(private val binding: ItemLogRecordBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(record: LogRecord) {
//            binding.textTitle.text = record.title
//            binding.textPlaceTime.text = "📍 ${record.address} | ${record.time}분"
//            binding.textTrash.text = "오늘의 총 쓰레기: ${record.trashCount}개"
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
//        val binding = ItemLogRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return RecordViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
//        holder.bind(records[position])
//    }
//
//    override fun getItemCount(): Int = records.size
//}

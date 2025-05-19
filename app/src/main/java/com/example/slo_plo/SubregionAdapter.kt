package com.example.slo_plo

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.slo_plo.databinding.ItemSubregionBinding

class SubregionAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    var selectedPosition: Int = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemSubregionBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            ItemSubregionBinding.bind(convertView)
        }

        binding.tvSubregionName.text = items[position]

        if (position == selectedPosition) {
            binding.tvSubregionName.setBackgroundColor(Color.parseColor("#D5EEFF"))
        } else {
            binding.tvSubregionName.setBackgroundResource(android.R.color.white)
        }

        return binding.root
    }
}

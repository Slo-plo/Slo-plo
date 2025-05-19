package com.example.slo_plo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.slo_plo.databinding.ItemRegionBinding

class RegionAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemRegionBinding
        val view: View

        if (convertView == null) {
            binding = ItemRegionBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as ItemRegionBinding
        }

        binding.tvRegionName.text = items[position]
        return view
    }
}

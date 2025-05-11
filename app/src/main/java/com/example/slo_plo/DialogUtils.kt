package com.example.slo_plo

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.example.slo_plo.databinding.DialogDefaultBinding

object DialogUtils {
    fun showConfirmDialog(
        context: Context,
        layoutInflater: LayoutInflater,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        val binding = DialogDefaultBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        binding.tvDefaultTitle.text = title
        binding.tvDefaultContent.text = message

        binding.btnDefaultYes.setOnClickListener {
            onConfirm()
            alertDialog.dismiss()
        }

        binding.btnDefaultNo.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
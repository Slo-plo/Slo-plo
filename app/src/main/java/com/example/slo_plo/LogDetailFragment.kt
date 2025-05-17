package com.example.slo_plo

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.slo_plo.databinding.DialogDefaultBinding
import com.example.slo_plo.databinding.FragmentLogDetailBinding
import com.example.slo_plo.model.LogRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LogDetailFragment : Fragment() {

    private var _binding: FragmentLogDetailBinding? = null
    private val binding get() = _binding!!

    private var logRecord: LogRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        logRecord = arguments?.getSerializable("logRecord") as? LogRecord

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnLogBack.setOnClickListener {
            findNavController().popBackStack()
        }

        logRecord?.let { record ->
            binding.tvLogTitle.text = record.title
            binding.tvLogDate.text = record.writeDateTime
            binding.tvStartAddress.text = "출발지점: ${record.startAddress}"
            binding.tvEndAddress.text = "도착지점: ${record.endAddress}"
            binding.tvLogTime.text = "시간 - ${record.time} 분"
            binding.tvLogDistance.text = "이동거리 - ${formatDistance(record.distance)}"
            binding.tvLogTrash.text = "수거한 쓰레기: ${record.trashCount}개"
            binding.tvLogContent.text = record.body
            // 이미지가 있을 경우
            val imageUrl = logRecord?.imageUrls?.firstOrNull()
            if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http")) {
                binding.ivLogImage.visibility = View.VISIBLE
                try {
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .into(binding.ivLogImage)
                } catch (e: Exception) {
                    Log.e("LogDetailFragment", "❌ Glide 이미지 로딩 실패", e)
                }
            } else {
                binding.ivLogImage.visibility = View.GONE
            }
        }


        binding.btnLogDelete.setOnClickListener {
            val dialogBinding = DialogDefaultBinding.inflate(layoutInflater)

            // 텍스트 동적 설정
            dialogBinding.tvDefaultTitle.text = "일지 삭제"
            dialogBinding.tvDefaultContent.text = "해당 일지를 정말 삭제하시겠습니까?"

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .create()

            // 아니오 버튼: 단순 닫기
            dialogBinding.btnDefaultNo.setOnClickListener {
                dialog.dismiss()
            }

            // 예 버튼: 실제 삭제 실행
            dialogBinding.btnDefaultYes.setOnClickListener {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                val docId = logRecord?.docId ?: return@setOnClickListener

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("plogging_logs")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "일지가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            }

            dialog.show()
        }


        // 공유 버튼
        binding.btnLogShare.setOnClickListener {
            val fragment = InstaShareFragment().apply {
                arguments = Bundle().apply {
                    putString("title", binding.tvLogTitle.text.toString())
                    putString("date", binding.tvLogDate.text.toString())
                    putString("content", binding.tvLogContent.text.toString())
                    putString("imageUrl", logRecord?.imageUrls?.firstOrNull() ?: "")
                }
            }

            childFragmentManager.beginTransaction()
                .add(R.id.insta_share_overlay_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format("%.1f km", meters / 1000)
        }
    }

}

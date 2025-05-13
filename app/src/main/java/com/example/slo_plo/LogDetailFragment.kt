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
            binding.tvLogTime.text = "시간 - ${record.time}"
            binding.tvLogDistance.text = "이동거리 - ${record.distance} m"
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
            Log.d("삭제 테스트", "버튼 클릭됨")
            Log.d("삭제 테스트", "docId: ${logRecord?.docId}")
            AlertDialog.Builder(requireContext())
                .setTitle("일지 삭제")
                .setMessage("일지를 삭제하시겠습니까?")
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("확인") { dialog, _ ->
                    // 실제 삭제 수행
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    val docId = logRecord?.docId ?: return@setPositiveButton

                    Log.d("LogDetailFragment", "삭제 시도할 문서 ID: $docId")

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .collection("plogging_logs")
                        .document(docId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "일지가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener {e ->
                            Toast.makeText(requireContext(), "삭제에 실패했습니다.: ${e.message}\"", Toast.LENGTH_SHORT).show()
                        }

                }
                .show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

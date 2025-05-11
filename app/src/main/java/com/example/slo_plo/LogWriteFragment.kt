package com.example.slo_plo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.slo_plo.DialogUtils.showConfirmDialog
import com.example.slo_plo.databinding.DialogDefaultBinding
import com.example.slo_plo.databinding.FragmentLogWriteBinding
import com.example.slo_plo.model.LogRecord
import com.example.slo_plo.utils.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class LogWriteFragment : Fragment() {

    private var _binding: FragmentLogWriteBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private var uid: String? = null

    // 카메라 권한 요청
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(requireContext(), "카메라 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    // 갤러리 권한 요청
    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openGallery()
        else Toast.makeText(requireContext(), "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    // 카메라 실행 결과 처리
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
            binding.ivLogSelected.apply {
                setImageBitmap(bitmap)
                visibility = View.VISIBLE
            }
        }
    }

    // 갤러리 실행 결과 처리
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivLogSelected.apply {
                setImageURI(selectedImageUri)
                visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid
        binding.etLogContent.movementMethod = ScrollingMovementMethod()
        
        // 뒤로가기: 홈 화면으로 이동
        binding.btnLogCancel.setOnClickListener {
            showConfirmDialog(
                context = requireContext(),
                layoutInflater = layoutInflater,
                title = "일지 작성 취소",
                message = "일지 작성을 취소하고\n홈화면으로 돌아가겠습니까?"
            ) {
                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("showSummary", true)
                findNavController().popBackStack()
            }
        }

        // 뒤로가기 버튼 클릭 이벤트를 감지하여 다이얼로그 띄우기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showConfirmDialog(
                context = requireContext(),
                layoutInflater = layoutInflater,
                title = "일지 작성 취소",
                message = "일지 작성을 취소하고\n홈화면으로 돌아가겠습니까?"
            ) {
                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("showSummary", true)
                findNavController().popBackStack()
            }
        }

        // 저장 버튼 클릭 리스너 안에서 uid 사용
        binding.bottomButtons.buttonSave.setOnClickListener {
            val userId = uid ?: run {
                Toast.makeText(requireContext(),
                    "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // LogRecord 생성
            val record = LogRecord(
                /* ... */
            )
            // userId 와 record 순서로 넘겨야 함
            FirestoreRepository.saveLogRecord(userId, record) { success ->
                if (success) { /* 저장 완료 */ }
                else { /* 저장 실패 */ }
            }
        }

        // 플로깅 기록 불러오기
        val args = requireArguments()
        val startAddr = args.getString("startAddress") ?: ""
        val endAddr = args.getString("endAddress") ?: ""
        val totalTime = args.getString("totalTime") ?: ""
        val totalDist = args.getString("totalDistance") ?: ""

        // 날짜 및 시간 설정
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(
            "yyyy년 M월 d일 E요일 HH시 mm분",
            Locale.KOREA
        )
        binding.tvLogDate.text = currentDateTime.format(formatter)

        // 플로깅 정보 반영
        binding.tvStartAddress.text = "출발지점: $startAddr"
        binding.tvEndAddress.text = "도착지점: $endAddr"
        binding.tvLogTime.text = "시간 - $totalTime"
        binding.tvLogDistance.text = "이동 거리 - $totalDist"

        // 카메라 버튼
        binding.btnBottom.btnLogCamera.setOnClickListener {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        // 갤러리 권한
        binding.btnBottom.btnLogGallery.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                android.Manifest.permission.READ_MEDIA_IMAGES
            } else {
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            }
            galleryPermissionLauncher.launch(permission)
        }

        // 저장 버튼
        binding.bottomButtons.buttonSave.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val dateId = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val logsRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("plogging_logs")
            val title = binding.etLogTitle.text.toString()
            val content = binding.etLogContent.text.toString()
            val trash = binding.etLogTrash.text.toString()

            showConfirmDialog(
                context = requireContext(),
                layoutInflater = layoutInflater,
                title = "일지 저장",
                message = "일지를 저장하시겠습니까?"
            ) {
                // 1. 기존 개수 세고
                logsRef
                    .whereEqualTo("dateId", dateId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val count = querySnapshot.size()
                        val newDocId = "${dateId}_${count + 1}"

                        // 2. 실제 저장할 데이터 구성
                        val record = LogRecord(
                            dateId = dateId,
                            startAddress = startAddr,
                            endAddress = endAddr,
                            time = totalTime.toIntOrNull() ?: 0,
                            distance = totalDist.toDoubleOrNull() ?: 0.0,
                            trashCount = trash.toIntOrNull() ?: 0,
                            title = title,
                            body = content,
                            imageUrls = emptyList()
                        )

                        // 3. 저장
                        logsRef.document(newDocId).set(record)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "저장 완료", Toast.LENGTH_SHORT).show()
                                findNavController().previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("needsRefresh", true)
                                findNavController().popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "저장 실패", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "기록 카운트 조회 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


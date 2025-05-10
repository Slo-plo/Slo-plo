package com.example.slo_plo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.slo_plo.databinding.FragmentLogWriteBinding
import com.example.slo_plo.model.LogRecord
import com.example.slo_plo.utils.FirestoreRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class LogWriteFragment : Fragment() {

    private var _binding: FragmentLogWriteBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

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
            binding.imageSelected.apply {
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
            binding.imageSelected.apply {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기: 홈 화면으로 이동
        binding.buttonBack.setOnClickListener {
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set("showSummary", true)
            findNavController().popBackStack()
        }

        // 플로깅 기록 불러오기
        val args = requireArguments()
        val startAddr = args.getString("startAddress") ?: ""
        val endAddr = args.getString("endAddress") ?: ""
        val totalTime = args.getString("totalTime") ?: ""
        val totalDist = args.getString("totalDistance") ?: ""

        // 날짜 설정
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일", Locale.KOREA)
        binding.textDate.text = currentDate.format(formatter)

        // 플로깅 정보 반영
        binding.textStartAddress.text = "출발지점: $startAddr"
        binding.textEndAddress.text = "도착지점: $endAddr"
        binding.textTime.text = "시간: $totalTime"
        binding.textDistance.text = "거리: $totalDist"

        // 카메라 버튼
        binding.bottomButtons.buttonCamera.setOnClickListener {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        // 갤러리 버튼
        binding.bottomButtons.buttonGallery.setOnClickListener {
            galleryPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // 갤러리 권한
        binding.bottomButtons.buttonGallery.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                android.Manifest.permission.READ_MEDIA_IMAGES
            } else {
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            }
            galleryPermissionLauncher.launch(permission)
        }


        // 저장 버튼
        binding.bottomButtons.buttonSave.setOnClickListener {
            // 1) LogRecord 객체 생성
            val currentDate = LocalDate.now()
            val dateId = currentDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val record = LogRecord(
                dateId       = dateId,
                startAddress = startAddr,
                endAddress   = endAddr,
                time         = totalTime.toIntOrNull() ?: 0,
                distance     = totalDist.toDoubleOrNull() ?: 0.0,
                trashCount   = args.getInt("trashCount", 0),
                title        = binding.editTitle.text.toString(),
                body         = binding.editContent.text.toString(),
                imageUrls    = emptyList()  // 이미지 업로드 후 URL 리스트로 대체
            )
            // 2) 저장
            FirestoreRepository.saveLogRecord(record) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "저장 완료", Toast.LENGTH_SHORT).show()
                    // 3) JournalFragment 에 갱신 요청
                    findNavController().previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("needsRefresh", true)
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "저장 실패", Toast.LENGTH_SHORT).show()
                }
            }
//            val title = binding.editTitle.text.toString()
//            val content = binding.editContent.text.toString()
//            Toast.makeText(requireContext(),
//                "제목: $title\n내용: $content",
//                Toast.LENGTH_SHORT
//            ).show()
        }
//        // 카메라 버튼
//        binding.buttonCamera.setOnClickListener {
//            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//        }
//
////        // 갤러리 버튼
////        binding.buttonGallery.setOnClickListener {
////            galleryPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
////        }
//        binding.buttonGallery.setOnClickListener {
//            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                android.Manifest.permission.READ_MEDIA_IMAGES
//            } else {
//                android.Manifest.permission.READ_EXTERNAL_STORAGE
//            }
//            galleryPermissionLauncher.launch(permission)
//        }
//
//
//        // 저장 버튼
//        binding.buttonSave.setOnClickListener {
//            val title = binding.editTitle.text.toString()
//            val content = binding.editContent.text.toString()
//            Toast.makeText(requireContext(), "저장됨\n제목: $title\n내용: $content", Toast.LENGTH_SHORT).show()
//        }
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


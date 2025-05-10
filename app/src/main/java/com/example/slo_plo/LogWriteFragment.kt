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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.slo_plo.databinding.DialogDefaultBinding
import com.example.slo_plo.databinding.FragmentLogWriteBinding
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

        binding.etLogContent.movementMethod = ScrollingMovementMethod()


        // 뒤로가기: 홈 화면으로 이동
        binding.btnLogCancel.setOnClickListener {
            showConfirmDialog(
                title = "일지 작성 취소",
                message = "일지 작성을 취소하고\n홈화면으로 돌아가겠습니까?"
            ) {
                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("showSummary", true)
                findNavController().popBackStack()
            }
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
        binding.tvLogDate.text = currentDate.format(formatter)

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
        binding.btnBottom.btnLogSave.setOnClickListener {
            val title = binding.etLogTitle.text.toString()
            val content = binding.etLogTitle.text.toString()
            val trash = binding.etLogTrash.text.toString()
            Toast.makeText(requireContext(),
                "제목: $title\n내용: $content 쓰레기 개수: $trash",
                Toast.LENGTH_SHORT
            ).show()
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

    fun showConfirmDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        val binding = DialogDefaultBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(requireContext())
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


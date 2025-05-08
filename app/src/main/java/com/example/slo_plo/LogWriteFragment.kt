package com.example.slo_plo

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class LogWriteFragment : Fragment() {

    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log_write, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 날짜 설정
        val textDate = view.findViewById<TextView>(R.id.text_date)
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일", Locale.KOREA)
        textDate.text = currentDate.format(formatter)

        // 임시 정보 세팅
        val textInfo = view.findViewById<TextView>(R.id.text_info)
        textInfo.text = """
            출발지점: 서울시 강남구 역삼동
            도착지점: 서울시 송파구 문정동
            시간: 35분
            거리: 3.2km
            쓰레기 개수: 7개
        """.trimIndent()

        // 뒤로가기
        view.findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 이미지뷰 참조
        imageView = view.findViewById(R.id.image_selected)

        // 카메라 버튼
        view.findViewById<ImageButton>(R.id.button_camera).setOnClickListener {
            requestCameraPermission()
        }

        // 갤러리 버튼
        view.findViewById<ImageButton>(R.id.button_gallery).setOnClickListener {
            galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // 저장 버튼
        view.findViewById<Button>(R.id.button_save).setOnClickListener {
            val title = view.findViewById<EditText>(R.id.edit_title).text.toString()
            val content = view.findViewById<EditText>(R.id.edit_content).text.toString()
            Toast.makeText(requireContext(), "저장됨\n제목: $title\n내용: $content", Toast.LENGTH_SHORT).show()
        }
    }

    // 갤러리 실행
    private fun openGallery() {
        galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }



    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }


    // 카메라 실행
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestGalleryPermissionAndOpenGallery() {
        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    // 결과 처리
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            imageView.setImageURI(selectedImageUri)
            imageView.visibility = View.VISIBLE
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
    }
}
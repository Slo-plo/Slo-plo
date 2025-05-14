package com.example.slo_plo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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

    // 플로깅 측정 데이터
    private var startAddr: String = ""
    private var endAddr: String = ""
    private var totalTime: String = ""
    private var totalDist: String = ""


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

        // 플로깅 기록 불러오기
        val args = requireArguments()
        startAddr = args.getString("startAddress") ?: ""
        endAddr = args.getString("endAddress") ?: ""
        totalTime = args.getString("totalTime") ?: ""
        totalDist = args.getString("totalDistance") ?: ""

        // 거리/시간 숫자로 변환
        val parsedMinutes = parseDurationToMinutes(totalTime)
        val parsedDistance = parseDistanceToMeters(totalDist)

        // 날짜 및 시간 설정
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(
            "yyyy년 M월 d일 E요일 HH시 mm분",
            Locale.KOREA
        )
        binding.tvLogDate.text = currentDateTime.format(formatter)

        // 플로깅 정보 반영 (UI 표시용 함수 사용)
        binding.tvStartAddress.text = "출발지점: $startAddr"
        binding.tvEndAddress.text = "도착지점: $endAddr"
        binding.tvLogTime.text = "시간 - ${formatTimeForDisplay(parsedMinutes)}"
        binding.tvLogDistance.text = "이동 거리 - ${formatDistanceForDisplay(parsedDistance)}"

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
            val currentDateTime = LocalDateTime.now()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Log.e("LogWriteFragment", "User ID is null. Are you logged in?")
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dateId = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val writeDateTime = currentDateTime.format(
                DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일 HH시 mm분", Locale.KOREA)
            )

            val title = binding.etLogTitle.text.toString()
            val content = binding.etLogContent.text.toString()
            val trash = binding.etLogTrash.text.toString()

            val logsRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid!!)
                .collection("plogging_logs")

            showConfirmDialog(
                context = requireContext(),
                layoutInflater = layoutInflater,
                title = "일지 저장",
                message = "일지를 저장하시겠습니까?"
            ) {
                // 1. 기존 개수 세고
                logsRef.get().addOnSuccessListener { querySnapshot ->
                    val sameDateDocIds = querySnapshot.documents
                        .mapNotNull { it.id }
                        .filter { it.startsWith("${dateId}_") }
                        .mapNotNull {
                            it.removePrefix("${dateId}_").toIntOrNull()
                        }

                    val nextNumber =
                        if (sameDateDocIds.isEmpty()) 1 else (sameDateDocIds.max()!! + 1)
                    val newDocId = "${dateId}_$nextNumber"

                    if (selectedImageUri != null) {
                        uploadImageToFirebase(
                            selectedImageUri!!,
                            onSuccess = { imageUrl ->
                                val record = LogRecord(
                                    dateId = dateId,
                                    startAddress = startAddr,
                                    endAddress = endAddr,
                                    time = totalTime.toIntOrNull() ?: 0,
                                    distance = totalDist.toDoubleOrNull() ?: 0.0,
                                    trashCount = trash.toIntOrNull() ?: 0,
                                    title = title,
                                    body = content,
                                    imageUrls = listOf(imageUrl),
                                    writeDateTime = writeDateTime,
                                    docId = newDocId
                                )

                                logsRef.document(newDocId).set(record)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "저장 완료",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("needsRefresh", true)
                                        findNavController().popBackStack()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "저장 실패",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            },
                            onFailure = {
                                Toast.makeText(requireContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                    } else {
                        val record = LogRecord(
                            dateId = dateId,
                            startAddress = startAddr,
                            endAddress = endAddr,
                            time = totalTime.toIntOrNull() ?: 0,
                            distance = totalDist.toDoubleOrNull() ?: 0.0,
                            trashCount = trash.toIntOrNull() ?: 0,
                            title = title,
                            body = content,
                            imageUrls = emptyList(),
                            writeDateTime = writeDateTime,
                            docId = newDocId
                        )

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
                }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "기록 카운트 조회 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }

    // 거리 문자열 → m 단위 Double
    private fun parseDistanceToMeters(distanceString: String): Double {
        val numberOnly = distanceString.replace("[^\\d.]".toRegex(), "")
        return numberOnly.toDoubleOrNull() ?: 0.0
    }

    // 시간 문자열 MM:SS → 전체 분(Int)
    private fun parseDurationToMinutes(timeString: String): Int {
        val parts = timeString.split(":").map { it.trim() }
        return if (parts.size == 2) {
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0
            minutes + if (seconds >= 30) 1 else 0  // 반올림 처리
        } else 0
    }


    private fun formatDistanceForDisplay(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format("%.2f km", meters / 1000)
        }
    }

    private fun formatTimeForDisplay(minutes: Int): String {
        return if (minutes < 60) {
            "${minutes}분"
        } else {
            val hours = minutes / 60
            val remainMinutes = minutes % 60
            if (remainMinutes == 0) "${hours}시간"
            else "${hours}시간 ${remainMinutes}분"
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

    private fun uploadImageToFirebase(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "images/${System.currentTimeMillis()}.jpg"
        val imageRef = storageRef.child(fileName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener {
                onFailure()
            }
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


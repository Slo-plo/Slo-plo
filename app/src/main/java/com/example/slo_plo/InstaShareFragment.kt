package com.example.slo_plo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.content.FileProvider
import com.example.slo_plo.databinding.FragmentInstaShareBinding
import java.io.File
import java.io.FileOutputStream

class InstaShareFragment : Fragment() {

    private var _binding: FragmentInstaShareBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstaShareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnInstaShare.setOnClickListener {
            // 흰색 배경을 비트맵으로 생성
            val backgroundBitmap = drawBackgroundBitmap()

            // 스티커 레이어 캡처
            val stickerBitmap = captureViewAsBitmap(binding.imgStickerLayout)

            // 배경과 스티커 각각 캐시로 저장
            val backgroundUri = saveBitmapToCache(backgroundBitmap, "background_image.png")
            val stickerUri = saveBitmapToCache(stickerBitmap, "sticker_image.png")

            // Instagram 스토리로 배경과 스티커 이미지 각각 공유
            shareToInstagramStory(backgroundUri, stickerUri)
        }
    }

    // 흰색 배경을 비트맵으로 나타내는 메서드
    private fun drawBackgroundBitmap(): Bitmap {
        val backgroundWidth = resources.displayMetrics.widthPixels
        val backgroundHeight = resources.displayMetrics.heightPixels

        val backgroundBitmap = Bitmap.createBitmap(backgroundWidth, backgroundHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(backgroundBitmap)

        // 배경색을 흰색으로 설정
        val bgColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        canvas.drawColor(bgColor)

        return backgroundBitmap
    }

    // 특정 View를 비트맵으로 변환하는 함수
    private fun captureViewAsBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    // 비트맵을 캐시에 저장하고 URI를 반환
    private fun saveBitmapToCache(bitmap: Bitmap, fileName: String): Uri {
        val file = File(requireContext().cacheDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    // Instagram 스토리로 배경과 스티커 이미지 각각 공유
    private fun shareToInstagramStory(backgroundUri: Uri, stickerUri: Uri) {
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            // 배경 이미지 URI 설정
            setDataAndType(backgroundUri, "image/png")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra("source_application", requireContext().packageName)

            // 스티커 이미지 URI 설정
            putExtra("interactive_asset_uri", stickerUri)
        }

        // Instagram에 권한 부여
        requireContext().grantUriPermission(
            "com.instagram.android",
            backgroundUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        requireContext().grantUriPermission(
            "com.instagram.android",
            stickerUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Instagram 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

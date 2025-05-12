package com.example.slo_plo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.slo_plo.databinding.FragmentInstaShareBinding
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

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

        binding.tvInstaShareName.text = "2025-05-11 플로깅 인증 (^ . ^)"
        binding.tvInstaShareDistance.text = "3.8 km 플로깅 성공 ~ !"

        binding.btnInstaShare.setOnClickListener {
            // SVG 배경을 비트맵으로 생성
            val backgroundBitmap = drawVectorBackgroundBitmap()

            // 스티커 레이어 캡처
            val stickerBitmap = captureViewAsBitmap(binding.imgStickerLayout)

            // 캐시 저장
            val backgroundUri = saveBitmapToCache(backgroundBitmap, "background_image_${UUID.randomUUID()}.png")
            val stickerUri = saveBitmapToCache(stickerBitmap, "sticker_image_${UUID.randomUUID()}.png")

            // 공유
            shareToInstagramStory(backgroundUri, stickerUri)
        }
    }

    // 🔁 여기만 바뀜: VectorDrawable을 비트맵으로 렌더링하는 함수
    private fun drawVectorBackgroundBitmap(): Bitmap {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.bg_instagram_story)
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)

        return bitmap
    }

    private fun captureViewAsBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

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

    private fun shareToInstagramStory(backgroundUri: Uri, stickerUri: Uri) {
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(backgroundUri, "image/png")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra("source_application", requireContext().packageName)
            putExtra("interactive_asset_uri", stickerUri)
        }

        requireContext().grantUriPermission("com.instagram.android", backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        requireContext().grantUriPermission("com.instagram.android", stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

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

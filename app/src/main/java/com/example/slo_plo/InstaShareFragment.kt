package com.example.slo_plo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
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

        binding.imgInstaShare.post {
            val width = binding.imgInstaShare.width
            binding.imgInstaShare.layoutParams.height = width
            binding.imgInstaShare.requestLayout()
        }

        binding.tvInstaShareDate.text = "2025일 05월 11일"
        binding.tvInstaShareContent.text = "오늘은 서울여대에서 플로깅을 했는데 정말 재미있었다~~!! 최대 몇 줄까지 표시하는 게 좋을지 모르겠어서 최대한 길게 써보고 있는데 세 줄이면 적당나나나나나난나나나나나나나나나나나나나나ㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㅏㄴ나나나나나나나나나나ㅏ나나나나나나나나나나난"
        binding.tvInstaShareTitle.text = "서울여대에서 플로깅을 했다"
        binding.imgInstaShare.setImageResource(R.drawable.img_temp_insta)

        binding.btnInstaShare.setOnClickListener {
            // UUID 생성 (중복 호출 방지)
            val uuid = UUID.randomUUID().toString()

            // SVG 배경을 비트맵으로 생성
            val backgroundBitmap = drawVectorBackgroundBitmap()

            // 스티커 레이어 캡처 (뷰가 완전히 그려진 후에 캡처)
            binding.imgStickerLayout.post {
                val stickerBitmap = captureViewAsBitmap(binding.imgStickerLayout)

                // 캐시 저장
                val backgroundUri = saveBitmapToCache(backgroundBitmap, "background_image_$uuid.png")
                val stickerUri = saveBitmapToCache(stickerBitmap, "sticker_image_$uuid.png")

                // 공유
                shareToInstagramStory(backgroundUri, stickerUri)
            }
        }
    }

    // VectorDrawable을 비트맵으로 렌더링하는 함수
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

    // 뷰를 비트맵으로 캡처하는 함수
    private fun captureViewAsBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    // 비트맵을 캐시 디렉토리에 저장하고 URI 반환
    private fun saveBitmapToCache(bitmap: Bitmap, fileName: String): Uri {
        val file = File(requireContext().cacheDir, fileName)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "파일 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    // Instagram 스토리 공유 함수
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

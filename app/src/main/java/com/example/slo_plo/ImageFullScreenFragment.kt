package com.example.slo_plo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView


class ImageFullScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_full_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imageView = view.findViewById<PhotoView>(R.id.imageViewPreview)
        val imageUrl = arguments?.getString("imageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .into(imageView)
        }

        // 클릭 시 뒤로가기
        imageView.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
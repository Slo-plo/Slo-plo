package com.example.slo_plo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.slo_plo.databinding.FragmentImageFullScreenBinding
import com.github.chrisbanes.photoview.PhotoView


class ImageFullScreenFragment : Fragment() {

    private var _binding: FragmentImageFullScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageFullScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imageUrl = arguments?.getString("imageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .into(binding.imageViewPreview)
        }

        // 클릭 시 뒤로가기
        binding.imageViewPreview.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
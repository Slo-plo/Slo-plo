package com.example.slo_plo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class LogIntroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_intro, container, false)

        val continueButton = view.findViewById<Button>(R.id.button_continue)
        continueButton.setOnClickListener {
            // 일지 작성 화면으로 이동
            parentFragmentManager.beginTransaction()
                //.replace(R.id.fragment_container, DiaryWriteFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_log_intro, container, false)
//    }

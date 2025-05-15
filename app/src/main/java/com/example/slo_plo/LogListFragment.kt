package com.example.slo_plo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.slo_plo.LogListAdapter
import com.example.slo_plo.databinding.FragmentLogListBinding
import com.example.slo_plo.model.LogRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LogListFragment : Fragment() {

    private var _binding: FragmentLogListBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val logRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("plogging_logs")

        logRef.orderBy("dateId", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val logList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(LogRecord::class.java)?.apply {
                        this.docId = doc.id
                    }
                }

                val adapter = LogListAdapter(logList) { record ->
                    val bundle = Bundle().apply {
                        putSerializable("logRecord", record)
                    }
                    findNavController().navigate(R.id.action_logList_to_logDetail, bundle)
                }

                binding.logListRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.logListRecycler.adapter = adapter
            }

        // 캘린더 버튼 클릭 → 캘린더 화면 이동
        binding.btnCalendar.setOnClickListener {
            findNavController().navigate(R.id.action_logList_to_calendar)
        }

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_logList_to_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

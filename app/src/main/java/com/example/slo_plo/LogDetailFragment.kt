package com.example.slo_plo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.slo_plo.model.LogRecord

class LogDetailFragment : Fragment() {

    private var logRecord: LogRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logRecord = arguments?.getSerializable("logRecord") as? LogRecord

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log_detail, container, false)
    }
}

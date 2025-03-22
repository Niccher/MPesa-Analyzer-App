package com.niccher.my_mpesa_analyzer.fragments.History

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.databinding.FragHistoryBinding

class Frag_History : Fragment() {

    private var _binding: FragHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_history, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.niccher.my_mpesa_analyzer.fragments.Info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.niccher.my_mpesa_analyzer.databinding.FragInfoBinding

class Frag_Info : Fragment() {

    private var _binding: FragInfoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragHomeVM =
            ViewModelProvider(this).get(Frag_Info_VM::class.java)

        _binding = FragInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textInfo
        fragHomeVM.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
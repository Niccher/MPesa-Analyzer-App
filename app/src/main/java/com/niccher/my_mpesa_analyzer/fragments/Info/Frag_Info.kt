package com.niccher.my_mpesa_analyzer.fragments.Info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.adapter.Adapter_Frag_Info
import com.niccher.my_mpesa_analyzer.databinding.FragInfoBinding

class Frag_Info : Fragment() {

    private var _binding: FragInfoBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: Frag_Info_VM
    private lateinit var recy_info: RecyclerView
    private lateinit var adapter: Adapter_Frag_Info

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recy_info = view.findViewById(R.id.info_more_Recycler)
        recy_info.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(this)[Frag_Info_VM::class.java]

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter = Adapter_Frag_Info(requireContext(), items)
            recy_info.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
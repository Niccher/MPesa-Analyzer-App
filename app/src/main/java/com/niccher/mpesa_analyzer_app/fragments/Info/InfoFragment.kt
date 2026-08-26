package com.niccher.mpesa_analyzer_app.fragments.Info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.adapter.InfoAdapter
import com.niccher.mpesa_analyzer_app.databinding.FragInfoBinding

class InfoFragment : Fragment() {

    private var _binding: FragInfoBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: InfoViewModel
    private lateinit var recy_info: RecyclerView
    private lateinit var adapter: InfoAdapter

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

        viewModel = ViewModelProvider(this)[InfoViewModel::class.java]

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter = InfoAdapter(requireContext(), items)
            recy_info.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
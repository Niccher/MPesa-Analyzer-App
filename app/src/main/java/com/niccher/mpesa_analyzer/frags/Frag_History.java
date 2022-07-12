package com.niccher.mpesa_analyzer.frags;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.niccher.mpesa_analyzer.R;

public class Frag_History extends Fragment {

    public Frag_History() {
        // Required empty public constructor
    }

    public static Frag_History newInstance(String param1, String param2) {
        Frag_History fragment = new Frag_History();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_history, container, false);
    }
}
package com.niccher.mpesa_analyzer.frags;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_adapter;
import com.niccher.mpesa_analyzer.konstants.Konstants;

import java.util.ArrayList;
import java.util.List;

public class Frag_Info extends Fragment {

    RecyclerView recyclerView;
    Konstants kon;

    public Frag_Info() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View solv= inflater.inflate(R.layout.frag_info, container, false);

        kon = new Konstants();

        final String[] mobileArray = {"Profile","Wishlist","Purchase History","Delivery Details"};
        final ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.part_info_item, mobileArray);
        final ListView listView = (ListView) solv.findViewById(R.id.account_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mobileArray[position] =="Profile"){
                    Log.e(kon.TAGGED, "onItemClick: Profile" );
                }

                if (mobileArray[position] =="Wishlist"){
                    Log.e(kon.TAGGED, "onItemClick: Wishlist" );
                }

                if (mobileArray[position] =="Purchase History"){
                    Log.e(kon.TAGGED, "onItemClick: Purchase History" );
                }

                if (mobileArray[position] =="Delivery Details"){
                    Log.e(kon.TAGGED, "onItemClick: Delivery Details" );
                }
            }
        });

        return solv;
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
}
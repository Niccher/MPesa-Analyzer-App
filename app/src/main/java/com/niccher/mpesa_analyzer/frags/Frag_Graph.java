package com.niccher.mpesa_analyzer.frags;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.niccher.mpesa_analyzer.R;

public class Frag_Graph extends Fragment {

    GraphView graph_line;

    public Frag_Graph() {
        // Required empty public constructor
    }

    public static Frag_Graph newInstance(String param1, String param2) {
        Frag_Graph fragment = new Frag_Graph();
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
        View grapher = inflater.inflate(R.layout.frag_graph, container, false);

        graph_line = (GraphView) grapher.findViewById(R.id.graph);
        drawGraph();
        return grapher;
    }

    public void drawGraph(){
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph_line.addSeries(series);
    }
}
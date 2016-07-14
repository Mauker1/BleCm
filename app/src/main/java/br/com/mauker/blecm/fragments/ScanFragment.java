package br.com.mauker.blecm.fragments;


import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import br.com.mauker.blecm.R;
import br.com.mauker.blecm.adapters.DeviceAdapter;

public class ScanFragment extends Fragment {

    private ListView lv_devices;
    private DeviceAdapter adapter;

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        lv_devices = (ListView) rootView.findViewById(R.id.fragment_scan_ll_servers);
        adapter = new DeviceAdapter(rootView.getContext(),new ArrayList<BluetoothDevice>());
        lv_devices.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doScan();
            }
        });

        return rootView;
    }



    private void doScan() {
        Snackbar.make(lv_devices, "Scanning.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}

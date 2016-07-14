package br.com.mauker.blecm.fragments;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.mauker.blecm.R;
import br.com.mauker.blecm.activities.ConsumerActivity;
import br.com.mauker.blecm.adapters.DeviceAdapter;

public class ScanFragment extends Fragment {

    private static final int SCAN_PERIOD = 20 * 1000;

    private ListView lv_devices;
    private DeviceAdapter adapter;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private Handler mHandler = new Handler();

    private Context mContext;

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
        final View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        mContext = rootView.getContext();

        mScanning = false;

        lv_devices = (ListView) rootView.findViewById(R.id.fragment_scan_ll_servers);
        adapter = new DeviceAdapter(rootView.getContext(),new ArrayList<BluetoothDevice>());
        lv_devices.setAdapter(adapter);

        lv_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = (BluetoothDevice) adapter.getItem(i);

                Toast.makeText(mContext,device.getName(),Toast.LENGTH_SHORT).show();

                // TODO - passar o treco pra tela.

                mContext.startActivity(new Intent(mContext, ConsumerActivity.class));
            }
        });

        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Snackbar.make(lv_devices, "No LE support.", Snackbar.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
    }

    /*
     * Begin a scan for new servers that advertise our
     * matching service.
     */
    private void startScan() {
        if (!mScanning) {
            adapter.clearAll();

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            mBluetoothAdapter.getBluetoothLeScanner().startScan(null, settings, mScanCallback);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            },SCAN_PERIOD);

            mScanning = true;

            Snackbar.make(lv_devices, "Scanning.", Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(lv_devices, "Scanning already started.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void stopScan() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        mScanning = false;
        Snackbar.make(lv_devices, "Scanning stopped.", Snackbar.LENGTH_LONG).show();
    }

    private void processResult(ScanResult result) {
        adapter.addDevice(result.getDevice());
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Snackbar.make(lv_devices, "Error while scanning - " + errorCode, Snackbar.LENGTH_SHORT).show();
        }
    };
}

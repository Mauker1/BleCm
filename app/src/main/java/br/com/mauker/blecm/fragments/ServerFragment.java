package br.com.mauker.blecm.fragments;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import br.com.mauker.blecm.R;
import br.com.mauker.blecm.ServerService;
import br.com.mauker.blecm.adapters.DeviceAdapter;
import br.com.mauker.blecm.events.BluetoothServiceEvent;

/**
 * Fragmento responsável por exibir os dispositivos que solicitaram dados desse dispositivo.
 * (Clientes requisitando informações do lado servidor dessa aplicação)
 */
public class ServerFragment extends Fragment {

    private ListView lv_devices;
    private DeviceAdapter adapter;

    private Context mContext;

    public ServerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        mContext.startService(new Intent(mContext, ServerService.class));
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mContext.stopService(new Intent(mContext,ServerService.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_server, container, false);

        mContext = rootView.getContext();

        lv_devices = (ListView) rootView.findViewById(R.id.fragment_server_ll_clients);
        adapter = new DeviceAdapter(rootView.getContext(),new ArrayList<BluetoothDevice>());
        lv_devices.setAdapter(adapter);

        return rootView;
    }

    /**
     * Método responsável por receber os eventos gerados pelo serviço background.
     * @param event Evento gerado pelo serviço background, podendo ser um cliente conectando-se ao
     *              servidor, ou desconectando-se.
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleBluetoothServiceEvent(BluetoothServiceEvent event) {
        switch (event.eventType) {
            case TYPE_ADD:
                adapter.addDevice(event.device);
                break;

            case TYPE_REMOVE:
                adapter.removeDevice(event.device);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

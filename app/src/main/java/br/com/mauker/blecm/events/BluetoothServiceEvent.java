package br.com.mauker.blecm.events;

import android.bluetooth.BluetoothDevice;

/**
 * Created by mauke on 13/07/2016.
 */
public class BluetoothServiceEvent {
    public BluetoothDevice device;
    public EventType eventType;

    public BluetoothServiceEvent(BluetoothDevice device, EventType eventType) {
        this.device = device;
        this.eventType = eventType;
    }
}

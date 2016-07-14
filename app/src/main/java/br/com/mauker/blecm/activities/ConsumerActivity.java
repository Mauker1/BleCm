package br.com.mauker.blecm.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import br.com.mauker.blecm.R;
import br.com.mauker.blecm.ServicesProfile;

public class ConsumerActivity extends AppCompatActivity {

    private static final String LOG_TAG = ConsumerActivity.class.getSimpleName();

    private BluetoothGatt mBluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer);

        BluetoothDevice bd = getIntent().getParcelableExtra("device");

        mBluetoothGatt = bd.connectGatt(getApplicationContext(),false,mGattCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    super.onConnectionStateChange(gatt,status,newState);
                    Log.i(LOG_TAG, "Connection state changed - " + newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(LOG_TAG, "Connected to GATT server.");
                        Log.i(LOG_TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(LOG_TAG, "Disconnected from GATT server.");
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        for (BluetoothGattService service : gatt.getServices()) {
                            Log.d(LOG_TAG, "Service: " + service.getUuid());

                            List<BluetoothGattCharacteristic> list = service.getCharacteristics();

                            Log.i(LOG_TAG,"List size: " + list.size());

                            if (ServicesProfile.SERVICE_WEATHER_UUID.equals(service.getUuid())) {
                                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                                Log.i(LOG_TAG,"Characteristics size: " + characteristics.size());

                                for (BluetoothGattCharacteristic c : characteristics) {
                                    Log.d(LOG_TAG,"Wohoo!");
                                    Log.d(LOG_TAG,c.getUuid().toString());
                                    gatt.readCharacteristic(c);
                                }
//                                gatt.readCharacteristic(service.getCharacteristic(ServicesProfile.CHARACTERISTIC_TEMPERATURE_UUID));
//                                gatt.readCharacteristic(service.getCharacteristic(ServicesProfile.CHARACTERISTIC_HUMIDITY_UUID));
                            }
                            else if (ServicesProfile.SERVICE_TIME_UUID.equals(service.getUuid())) {
//                                gatt.readCharacteristic(service.getCharacteristic(ServicesProfile.CHARACTERISTIC_MILLIS_UUID));
                            }

                        }
                    } else {
                        Log.w(LOG_TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.d(LOG_TAG,"onCharacteristicRead() called.");
                    Log.d(LOG_TAG,"UUID: " + characteristic.getUuid());
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (ServicesProfile.CHARACTERISTIC_TEMPERATURE_UUID.equals(characteristic.getUuid())) {
                            Log.i(LOG_TAG,"Temperature: " + ServicesProfile.unsignedIntFromBytes(characteristic.getValue()));
                        }
                        if (ServicesProfile.CHARACTERISTIC_HUMIDITY_UUID.equals(characteristic.getUuid())) {
                            Log.i(LOG_TAG,"Humidity: " + ServicesProfile.unsignedIntFromBytes(characteristic.getValue()));
                        }
                        if (ServicesProfile.CHARACTERISTIC_MILLIS_UUID.equals(characteristic.getUuid())) {
                            Log.i(LOG_TAG,"Time: " + ServicesProfile.unsignedIntFromBytes(characteristic.getValue()));
                        }

                    }
                }
            };
}

package br.com.mauker.blecm;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import br.com.mauker.blecm.events.BluetoothServiceEvent;
import br.com.mauker.blecm.events.EventType;

/**
 * Created by daniel on 13/07/16.
 */
public class ServerService extends Service {
    private final static String TAG = ServerService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;

    private ArrayList<BluetoothDevice> mConnectedDevices;
    private ArrayAdapter<BluetoothDevice> mConnectedDevicesAdapter;

    private Handler mHandler = new Handler();

    public ServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "in onCreate");

        ListView list = new ListView(this);


        mConnectedDevices = new ArrayList<>();
        mConnectedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mConnectedDevices);
        list.setAdapter(mConnectedDevicesAdapter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        initialize();

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "in onDestroy");
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    private void initialize(){


        mConnectedDevices = new ArrayList<>();
        //mConnectedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mConnectedDevices);

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();


        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            stopSelf();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        /*
         * Check for advertising support. Not all devices are enabled to advertise
         * Bluetooth LE data.
         */
        if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Toast.makeText(this, "No Advertising Support.", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);

        initServer();
        startAdvertising();

    }

    private void initServer() {
        BluetoothGattService service = new BluetoothGattService(ServicesProfile.SERVICE_WEATHER_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattService serviceTime = new BluetoothGattService(ServicesProfile.SERVICE_TIME_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic temperatureCharacteristic =
                new BluetoothGattCharacteristic(ServicesProfile.CHARACTERISTIC_TEMPERATURE_UUID,
                        //Read-only characteristic, supports notifications
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic humidityCharacteristic =
                new BluetoothGattCharacteristic(ServicesProfile.CHARACTERISTIC_HUMIDITY_UUID,
                        //Read-only characteristic, supports notifications
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic millisCharacteristic =
                new BluetoothGattCharacteristic(ServicesProfile.CHARACTERISTIC_MILLIS_UUID,
                        //Read-only characteristic, supports notifications
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        Log.d(TAG,"Add: " + service.addCharacteristic(temperatureCharacteristic));
        Log.d(TAG,"Add: " + service.addCharacteristic(humidityCharacteristic));

        Log.d(TAG,"Add: " + serviceTime.addCharacteristic(millisCharacteristic));

        mGattServer.addService(service);
        mGattServer.addService(serviceTime);
    }

    private void shutdownServer() {
        mHandler.removeCallbacks(mNotifyRunnable);

        if (mGattServer == null) return;

        mGattServer.close();
    }

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            notifyConnectedDevices();
            mHandler.postDelayed(this, 2000);
        }
    };

    /*
     * Callback handles all incoming requests from GATT clients.
     * From connections to read/write requests.
     */
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.i(TAG, "onConnectionStateChange "
                    + ServicesProfile.getStatusDescription(status)+" "
                    + ServicesProfile.getStateDescription(newState));

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                postDeviceChange(device, true);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                postDeviceChange(device, false);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device,
                                                int requestId,
                                                int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.i(TAG, "onCharacteristicReadRequest " + characteristic.getUuid().toString());

            if (ServicesProfile.CHARACTERISTIC_TEMPERATURE_UUID.equals(characteristic.getUuid())) {
                mGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        getTemperatureValue());
            }

            if (ServicesProfile.CHARACTERISTIC_HUMIDITY_UUID.equals(characteristic.getUuid())) {
                mGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        getHumidityValue());
            }

            if (ServicesProfile.CHARACTERISTIC_MILLIS_UUID.equals(characteristic.getUuid())) {
                mGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        getTimeMillisValue());
            }

            /*
             * Unless the characteristic supports WRITE_NO_RESPONSE,
             * always send a response back for any request.
             */
            mGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    null);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite,
                                                 boolean responseNeeded,
                                                 int offset,
                                                 byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.i(TAG, "onCharacteristicWriteRequest "+characteristic.getUuid().toString());

        }
    };

    /*
     * Initialize the advertiser
     */
    private void startAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(ServicesProfile.SERVICE_WEATHER_UUID))
                .addServiceUuid(new ParcelUuid(ServicesProfile.SERVICE_TIME_UUID))
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }

    /*
     * Terminate the advertiser
     */
    private void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    /*
     * Callback handles events from the framework describing
     * if we were successful in starting the advertisement requests.
     */
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "Peripheral Advertise Started.");
            postStatusMessage("GATT Server Ready");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "Peripheral Advertise Failed: "+errorCode);
            postStatusMessage("GATT Server Error "+errorCode);
        }
    };

    private void postStatusMessage(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //setTitle(message);
            }
        });
    }

    private void postDeviceChange(final BluetoothDevice device, final boolean toAdd) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //This will add the item to our list and update the adapter at the same time.
                if (toAdd) {
                    mConnectedDevicesAdapter.add(device);
                    EventBus.getDefault().postSticky(new BluetoothServiceEvent(device, EventType.TYPE_ADD));
                } else {
                    mConnectedDevicesAdapter.remove(device);
                    EventBus.getDefault().postSticky(new BluetoothServiceEvent(device, EventType.TYPE_REMOVE));
                }

                //Trigger our periodic notification once devices are connected
                mHandler.removeCallbacks(mNotifyRunnable);
                if (!mConnectedDevices.isEmpty()) {
                    mHandler.post(mNotifyRunnable);
                }
            }
        });
    }

     /* Storage and access to local characteristic data */

    private void notifyConnectedDevices() {
        for (BluetoothDevice device : mConnectedDevices) {
            BluetoothGattCharacteristic temperatureCharacteristic = mGattServer.getService(ServicesProfile.SERVICE_WEATHER_UUID)
                    .getCharacteristic(ServicesProfile.CHARACTERISTIC_TEMPERATURE_UUID);

            BluetoothGattCharacteristic humidityCharacteristic = mGattServer.getService(ServicesProfile.SERVICE_WEATHER_UUID)
                    .getCharacteristic(ServicesProfile.CHARACTERISTIC_HUMIDITY_UUID);

            BluetoothGattCharacteristic timeMillisCharacteristic = mGattServer.getService(ServicesProfile.SERVICE_TIME_UUID)
                    .getCharacteristic(ServicesProfile.CHARACTERISTIC_MILLIS_UUID);


            temperatureCharacteristic.setValue(getTemperatureValue());
            humidityCharacteristic.setValue(getHumidityValue());

            timeMillisCharacteristic.setValue(getTimeMillisValue());

            mGattServer.notifyCharacteristicChanged(device, temperatureCharacteristic, false);
            mGattServer.notifyCharacteristicChanged(device, humidityCharacteristic, false);
            mGattServer.notifyCharacteristicChanged(device, timeMillisCharacteristic, false);
        }
    }


    private final Object mLock = new Object();

    private byte[] getTemperatureValue() {
        synchronized (mLock) {
            return ServicesProfile.getTemperatureValue();
        }
    }

    private byte[] getHumidityValue() {
        synchronized (mLock) {
            return ServicesProfile.getHumidityValue();
        }
    }

    private byte[] getTimeMillisValue() {
        return ServicesProfile.getTime();
    }


}


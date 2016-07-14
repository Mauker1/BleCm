package br.com.mauker.blecm;

/**
 * Created by daniel on 13/07/16.
 */

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import br.com.mauker.blecm.utils.RandomUtils;
import br.com.mauker.blecm.utils.UuidUtils;

class ServicesProfile {
    /* Unique ids are randomly chosen. */

    // Services UUID to expose our characteristics
    public static UUID SERVICE_WEATHER_UUID = UuidUtils.uuidFromShortCode32("FAFAFAFA");
    public static UUID SERVICE_TIME_UUID = UuidUtils.uuidFromShortCode32("FBFBFBFB");

    // Read-only characteristic providing number of elapsed seconds since offset
    public static UUID CHARACTERISTIC_TEMPERATURE_UUID = UuidUtils.uuidFromShortCode32("E0E00000");
    public static UUID CHARACTERISTIC_HUMIDITY_UUID = UuidUtils.uuidFromShortCode32("E0E10000");

    public static UUID CHARACTERISTIC_MILLIS_UUID = UuidUtils.uuidFromShortCode32("E0E20000");

    public static String getStateDescription(int state) {
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                return "Connected";
            case BluetoothProfile.STATE_CONNECTING:
                return "Connecting";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "Disconnected";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "Disconnecting";
            default:
                return "Unknown State " + state;
        }
    }

    public static String getStatusDescription(int status) {
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                return "SUCCESS";
            default:
                return "Unknown Status " + status;
        }
    }

    public static byte[] getTemperatureValue() {
        int value = RandomUtils.randInt(26,45);
        return bytesFromInt(value);
    }

    public static byte[] getHumidityValue() {
        int value = RandomUtils.randInt(1,100);
        return bytesFromInt(value);
    }

    public static byte[] getTime() {
        long millis = System.currentTimeMillis();
        return bytesFromLong(millis);
    }

    public static int unsignedIntFromBytes(byte[] raw) {
        if (raw.length < 4) throw new IllegalArgumentException("Cannot convert raw data to int");

        return ((raw[0] & 0xFF)
                + ((raw[1] & 0xFF) << 8)
                + ((raw[2] & 0xFF) << 16)
                + ((raw[3] & 0xFF) << 24));
    }

    public static byte[] bytesFromInt(int value) {
        //Convert result into raw bytes. GATT APIs expect LE order
        return ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
    }

    public static byte[] bytesFromLong(long value) {
        //Convert result into raw bytes. GATT APIs expect LE order
        return ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(value)
                .array();
    }
}

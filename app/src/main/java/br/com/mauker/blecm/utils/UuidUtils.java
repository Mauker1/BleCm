package br.com.mauker.blecm.utils;

import java.util.UUID;

/**
 * Created by daniel on 13/07/16.
 */
public class UuidUtils {
    public static final String baseBluetoothUuidPostfix = "0000-1000-8000-00805F9B34FB";

    public static UUID uuidFromShortCode16(String shortCode16) {
        return UUID.fromString("0000" + shortCode16 + "-" + baseBluetoothUuidPostfix);
    }

    public static UUID uuidFromShortCode32(String shortCode32) {
        return UUID.fromString(shortCode32 + "-" + baseBluetoothUuidPostfix);
    }
}
package com.sxr.sdk.ble.keepfit.client;

import com.sxr.sdk.ble.keepfit.service.BluetoothLeService;
public class SampleBleService extends BluetoothLeService {
    public static String ECG_SWITCH = "ECG_SWITCH";
    public static String ECG_VALUE = "ECG_VALUE";
    public static String FACTORYMODE_TEST_SWITCH = "FACTORYMODE_TEST_SWITCH";
    public static String FACTORYMODE_TEST_VALUE = "FACTORYMODE_TEST_VALUE";

    @Override
    public void onCreate() {

        super.onCreate();
    }
}

package com.sxr.sdk.ble.keepfit.client;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class CheckUtil {
    public static boolean checkBluetooth(Context context, String hint){
        boolean bHas = true;
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            bHas = false;
            Toast.makeText(context, hint, Toast.LENGTH_LONG).show();
        }else if (!mBluetoothAdapter.isEnabled()) {
            bHas = false;
            new AlertDialog.Builder(context).setMessage(hint)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (!CheckUtil.checkPermission(context, context.getString(R.string.permission_ble_connect), Manifest.permission.BLUETOOTH_CONNECT, 0))
                                    return;
                            }
                            mBluetoothAdapter.enable();
                        }
                    }).setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                CheckUtil.checkPermission(context, context.getString(R.string.permission_ble_scan), Manifest.permission.BLUETOOTH_SCAN, 0);
                CheckUtil.checkPermission(context, context.getString(R.string.permission_ble_connect), Manifest.permission.BLUETOOTH_CONNECT, 0);
            }
        }
            return bHas;
    }


    public static final int CODE_CALENDAR = 0;
    public static final int CODE_CAMERA = 1;
    public static final int CODE_CONTACTS = 2;
    public static final int CODE_LOCATION = 3;
    public static final int CODE_AUDIO = 4;
    public static final int CODE_PHONE = 5;
    public static final int CODE_SMS = 6;
    public static final int CODE_STORAGE = 7;
    public static final int CODE_SENSORS = 8;

    private static String[][] permissionGroup = new String[][]{
            {Manifest.permission.CAMERA},
            {Manifest.permission.READ_CONTACTS},
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
            {Manifest.permission.RECORD_AUDIO},
            {Manifest.permission.ANSWER_PHONE_CALLS},
            {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
            {Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.ANSWER_PHONE_CALLS},
            {Manifest.permission.SEND_SMS},
            {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
    };

    public static boolean hasPermission(Context context, String hint, String permission, int code){
        boolean bHas = true;
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            bHas = false;
        }
        return bHas;
    }

    public static boolean checkPermission(Context context, String hint, String permission, int code){
        boolean bHas = true;
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            bHas = false;

            String[] permissions = new String[]{permission};
            for (int i = 0; i < permissionGroup.length; i++) {
                for (int j = 0; j < permissionGroup[i].length; j++) {
                    if(permission.equalsIgnoreCase(permissionGroup[i][j])) {
                        permissions = permissionGroup[i];
                        break;
                    }
                }
            }

            ActivityCompat.requestPermissions((Activity)context, permissions, code);

        }
        return bHas;
    }

    public static boolean checkPermission(Fragment fragment, String hint, String permission, int code){
        Context context = fragment.getContext();
        boolean bHas = true;
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            bHas = false;
            if(!hint.isEmpty()) {
                new AlertDialog.Builder(fragment.getContext())
                        .setMessage(hint)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                String[] permissions = new String[]{permission};
                                for (int i = 0; i < permissionGroup.length; i++) {
                                    for (int j = 0; j < permissionGroup[i].length; j++) {
                                        if(permission.equalsIgnoreCase(permissionGroup[i][j])) {
                                            permissions = permissionGroup[i];
                                            break;
                                        }
                                    }
                                }

                                fragment.requestPermissions(permissions, code);
                            }
                        }).show();
            }
            else {
                String[] permissions = new String[]{permission};
                for (int i = 0; i < permissionGroup.length; i++) {
                    for (int j = 0; j < permissionGroup[i].length; j++) {
                        if (permission.equalsIgnoreCase(permissionGroup[i][j])) {
                            permissions = permissionGroup[i];
                            break;
                        }
                    }
                }

                fragment.requestPermissions(permissions, code);
            }
        }
        return bHas;
    }


    public static boolean checkNetwork(Context context, String hint) {
        boolean bHas = true;
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                bHas = mNetworkInfo.isAvailable();
            }else{
                bHas = false;
            }
        }
        if(!bHas)
            Toast.makeText(context, hint, Toast.LENGTH_LONG).show();
        return bHas;
    }

    public static boolean checkGps(final Context context, String hint) {
        boolean bHas = true;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps && !network) {
            bHas = false;
            new AlertDialog.Builder(context).setMessage(hint)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show();
        }
        return bHas;
    }

    public static boolean hasPermission(Context context, String permission){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }
}

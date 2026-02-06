package com.sxr.sdk.ble.keepfit.client;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.luck.picture.lib.basic.PictureSelectionModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.engine.CropFileEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.MediaExtraInfo;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.MediaUtils;
import com.luck.picture.lib.utils.PictureFileUtils;
import com.luck.picture.lib.utils.StyleUtils;
import com.sxr.sdk.ble.keepfit.aidl.AlarmInfoItem;
import com.sxr.sdk.ble.keepfit.aidl.BleClientOption;
import com.sxr.sdk.ble.keepfit.aidl.ContactInfo;
import com.sxr.sdk.ble.keepfit.aidl.ContactInfoItem;
import com.sxr.sdk.ble.keepfit.aidl.DeviceProfile;
import com.sxr.sdk.ble.keepfit.aidl.ECardInfo;
import com.sxr.sdk.ble.keepfit.aidl.ECardInfoItem;
import com.sxr.sdk.ble.keepfit.aidl.IRemoteService;
import com.sxr.sdk.ble.keepfit.aidl.IServiceCallback;
import com.sxr.sdk.ble.keepfit.aidl.SmsRspInfo;
import com.sxr.sdk.ble.keepfit.aidl.SmsRspInfoItem;
import com.sxr.sdk.ble.keepfit.aidl.UserProfile;
import com.sxr.sdk.ble.keepfit.aidl.Weather;
import com.sxr.sdk.ble.keepfit.client.dialmarket.DialMarketActivity;
import com.sxr.sdk.ble.keepfit.client.ecg.EcgTestActivity;
import com.sxr.sdk.ble.keepfit.client.ecg.ShareUtil;
import com.sxr.sdk.ble.keepfit.client.factory.FactoryModeTestActivity;
import com.sxr.sdk.ble.keepfit.client.pictureselector.GlideEngine;
import com.sxr.sdk.ble.keepfit.client.pictureselector.ImageLoaderUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends ComponentActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private IRemoteService mService;
    private boolean mIsBound = false;
    private int countStep = 0;
    private String data = "";
    private LinearLayout llConnect;
    private String mClassicBtDeviceBtName = "";

    private boolean bSaveLog = true;
    private boolean bAuthSuccess = false; // Flag to keep UI visible after auth 200

    private static int ota_mode_firmware = 1;
    private static int ota_mode_dial = 2;
    private static int ota_mode_wallpaper = 3;

    private int deviceColorType = 0;
    private int deviceShapeType = 0;
    private int deviceWatchWidth = 0;
    private int deviceWatchHeight = 0;
    private int deviceWatchUnitWidth = 0;
    private int deviceWatchReviewWidth = 0;
    private int deviceWatchReviewHeight = 0;

    public SharedPreferences sharedPreferences;

    private PictureSelectorStyle selectorStyle;
    /**
     * é…åˆ¶UCropï¼Œå¯æ ¹æ®éœ€æ±‚è‡ªæˆ‘æ‰©å±•
     *
     * @return
     */
    private UCrop.Options buildOptions(int aspect_ratio_x, int aspect_ratio_y) {
        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(false);
        options.setShowCropFrame(true);
        options.setShowCropGrid(false);
        options.setCircleDimmedLayer(false);
        options.withAspectRatio(aspect_ratio_x, aspect_ratio_y);
        options.withMaxResultSize(aspect_ratio_x, aspect_ratio_y);
//		options.setCropOutputPathDir(getSandboxPath());
        options.isCropDragSmoothToCenter(false);
//		options.setSkipCropMimeType(getNotSupportCrop());
        options.isForbidCropGifWebp(true);
        options.isForbidSkipMultipleCrop(true);
        options.setMaxScaleMultiplier(100);
        if (selectorStyle != null && selectorStyle.getSelectMainStyle().getStatusBarColor() != 0) {
            SelectMainStyle mainStyle = selectorStyle.getSelectMainStyle();
            boolean isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack();
            int statusBarColor = mainStyle.getStatusBarColor();
            options.isDarkStatusBarBlack(isDarkStatusBarBlack);
            if (StyleUtils.checkStyleValidity(statusBarColor)) {
                options.setStatusBarColor(statusBarColor);
                options.setToolbarColor(statusBarColor);
            } else {
                options.setStatusBarColor(ContextCompat.getColor(this, R.color.ps_color_grey));
                options.setToolbarColor(ContextCompat.getColor(this, R.color.ps_color_grey));
            }
            TitleBarStyle titleBarStyle = selectorStyle.getTitleBarStyle();
            if (StyleUtils.checkStyleValidity(titleBarStyle.getTitleTextColor())) {
                options.setToolbarWidgetColor(titleBarStyle.getTitleTextColor());
            } else {
                options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.ps_color_white));
            }
        } else {
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.ps_color_grey));
            options.setToolbarColor(ContextCompat.getColor(this, R.color.ps_color_grey));
            options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.ps_color_white));
        }
        return options;
    }

    /**
     * è£å‰ªå¼•æ“Ž
     *
     * @return
     */
    private ImageFileCropEngine getCropFileEngine(int width, int height) {
        return new ImageFileCropEngine(width, height);
    }

    /**
     * è‡ªå®šä¹‰è£å‰ª
     */
    private class ImageFileCropEngine implements CropFileEngine {
        protected int aspect_ratio_x;
        protected int aspect_ratio_y;
        public ImageFileCropEngine(int ratio_x, int ratio_y) {
            this.aspect_ratio_x = ratio_x;
            this.aspect_ratio_y = ratio_y;
        }

        @Override
        public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
            UCrop.Options options = buildOptions(aspect_ratio_x, aspect_ratio_y);
            UCrop uCrop = UCrop.of(srcUri, destinationUri, dataSource);
            uCrop.withOptions(options);
            uCrop.setImageEngine(new UCropImageEngine() {
                @Override
                public void loadImage(Context context, String url, ImageView imageView) {
                    if (!ImageLoaderUtils.assertValidRequest(context)) {
                        return;
                    }
                    Glide.with(context).load(url).override(180, 180).into(imageView);
                }

                @Override
                public void loadImage(Context context, Uri url, int maxWidth, int maxHeight, UCropImageEngine.OnCallbackListener<Bitmap> call) {
                    Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (call != null) {
                                call.onCall(resource);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            if (call != null) {
                                call.onCall(null);
                            }
                        }
                    });
                }
            });
            uCrop.start(fragment.requireActivity(), fragment, requestCode);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_SHORT).show();

            mService = IRemoteService.Stub.asInterface(service);
            try {
                mService.registerCallback(mServiceCallback);
                mService.openSDKLog(bSaveLog, CommonAttributes.P_LOG_PATH, "demo.log");

                boolean isConnected = callRemoteIsConnected();

                if (isConnected == false) {
                    btBind.setEnabled(false);
                    btUnbind.setEnabled(true);
                    btScan.setEnabled(true);
                    btConnect.setEnabled(false);
                    btDisconnect.setEnabled(false);
                    llConnect.setVisibility(View.GONE);
                } else {
                    int authrize = callRemoteIsAuthrize();
                    if (authrize == 200) {
                        macid = callRemoteGetConnectedDevice();

                        btBind.setEnabled(false);
                        btUnbind.setEnabled(true);
                        btScan.setEnabled(true);
                        btConnect.setEnabled(false);
                        btDisconnect.setEnabled(true);
                        llConnect.setVisibility(View.VISIBLE);
                    }
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service disconnected", Toast.LENGTH_SHORT).show();

            btBind.setEnabled(true);
            btUnbind.setEnabled(false);
            btScan.setEnabled(false);
            btConnect.setEnabled(false);
            btDisconnect.setEnabled(false);
            llConnect.setVisibility(View.GONE);
            mService = null;
        }
    };

    private IServiceCallback mServiceCallback = new IServiceCallback.Stub() {
        @Override
        public void onConnectStateChanged(int state) throws RemoteException {
            showToast("onConnectStateChanged", macid + " state " + state);
            updateConnectState(state);
        }


        @Override
        public void onScanCallback(final String deviceName, final String deviceMacAddress, final int rssi, String ver, String cid, String did, String bindFlag, String bindState)
                throws RemoteException {
            saveLog(String.format("onScanCallback <%1$s>[%2$s](%3$d)", deviceName, deviceMacAddress, rssi));

            if (nearbyItemList == null || !bScan)
                return;

            if(bAutoEcg && !sAutoName.isEmpty() && !deviceName.contains(sAutoName))
                return;

            Iterator<BleDeviceItem> iter = nearbyItemList.iterator();
            BleDeviceItem item = null;
            boolean bExist = false;
            while (iter.hasNext()) {
                item = (BleDeviceItem) iter.next();
                if (item.getBleDeviceAddress().equalsIgnoreCase(deviceMacAddress)) {
                    bExist = true;
                    item.setRssi(rssi);
                    break;
                }
            }

            if (bExist == false) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BleDeviceItem item = new BleDeviceItem(deviceName, deviceMacAddress, "", "", rssi, "");
                        nearbyItemList.add(item);
                        Collections.sort(nearbyItemList, new ComparatorBleDeviceItem());

                        nearbyListAdapter.notifyDataSetChanged();
                    }
                });
            }

//            Message msg = new Message();
//            scanDeviceHandler.sendMessage(msg);
        }


        @Override
        public void onSetNotify(int result) throws RemoteException {
            showToast("onSetNotify", String.valueOf(result));
        }

        @Override
        public void onSetUserInfo(int result) throws RemoteException {
            showToast("onSetUserInfo", "" + result);
        }

        @Override
        public void onAuthSdkResult(int errorCode) throws RemoteException {
            showToast("onAuthSdkResult", errorCode + "");
        }

        @Override
        public void onGetDeviceTime(int result, String time) throws RemoteException {
            showToast("onGetDeviceTime", String.valueOf(time));
        }

        @Override
        public void onSetDeviceTime(int arg0) throws RemoteException {
            showToast("onSetDeviceTime", arg0 + "");
        }

        @Override
        public void onSetDeviceInfo(int arg0) throws RemoteException {
            showToast("onSetDeviceInfo", arg0 + "");
        }


        public void onAuthDeviceResult(int arg0) throws RemoteException {
            showToast("onAuthDeviceResult", arg0 + "");
            if (arg0 == 200) {
                bAuthSuccess = true;
                // Keep UI visible after successful auth
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btDisconnect.setEnabled(true);
                        llConnect.setVisibility(View.VISIBLE);
                    }
                });
            }
        }


        @Override
        public void onSetAlarm(int arg0) throws RemoteException {
            showToast("onSetAlarm", arg0 + "");
        }

        @Override
        public void onSendVibrationSignal(int arg0) throws RemoteException {
            showToast("onSendVibrationSignal", "result:" + arg0);
        }

        @Override
        public void onGetDeviceBatery(int arg0, int arg1)
                throws RemoteException {
            showToast("onGetDeviceBatery", "batery:" + arg0 + ", statu " + arg1);
        }


        @Override
        public void onSetDeviceMode(int arg0) throws RemoteException {
            showToast("onSetDeviceMode", "result:" + arg0);
        }

        @Override
        public void onSetHourFormat(int arg0) throws RemoteException {
            showToast("onSetHourFormat ", "result:" + arg0);

        }

        @Override
        public void setAutoHeartMode(int arg0) throws RemoteException {
            showToast("setAutoHeartMode ", "result:" + arg0);
        }


        @Override
        public void onGetCurSportData(int type, long timestamp, int step, int distance,
                                      int cal, int cursleeptime, int totalrunningtime, int steptime) throws RemoteException {
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String time = sdf.format(date);
            showToast("onGetCurSportData", "type : " + type + " , time :" + time + " , step: " + step + ", distance :" + distance + ", cal :" + cal + ", cursleeptime :" + cursleeptime + ", totalrunningtime:" + totalrunningtime);
        }

        @Override
        public void onGetSenserData(int result, long timestamp, int heartrate, int sleepstatu)
                throws RemoteException {
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String time = sdf.format(date);
            showToast("onGetSenserData", "result: " + result + ",time:" + time + ",heartrate:" + heartrate + ",sleepstatu:" + sleepstatu);

        }


        @Override
        public void onGetDataByDay(int type, long timestamp, int step, int heartrate)
                throws RemoteException {
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String recorddate = sdf.format(date);
            showToast("onGetDataByDay", "type:" + type + ",time::" + recorddate + ",step:" + step + ",heartrate:" + heartrate);
            if (type == 2) {
                sleepcount++;
            }
        }

        @Override
        public void onGetDataByDayEnd(int type, long timestamp) throws RemoteException {
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String recorddate = sdf.format(date);
            showToast("onGetDataByDayEnd", type + " time:" + recorddate + ",sleepcount:" + sleepcount);
            sleepcount = 0;
        }


        @Override
        public void onSetPhontMode(int arg0) throws RemoteException {
            showToast("onSetPhontMode", "result:" + arg0);
        }


        @Override
        public void onSetSleepTime(int arg0) throws RemoteException {
            showToast("onSetSleepTime", "result:" + arg0);
        }


        @Override
        public void onSetIdleTime(int arg0) throws RemoteException {
            showToast("onSetIdleTime", "result:" + arg0);
        }


        @Override
        public void onGetDeviceInfo(int version, String macaddress, String vendorCode,
                                    String productCode, int result) throws RemoteException {
            showToast("onGetDeviceInfo", "version :" + version + ",macaddress : " + macaddress + ",vendorCode : " + vendorCode + ",productCode :" + productCode + " , CRCresult :" + result);
            String fwVer = vendorCode + productCode + "V" + version;
            sharedPreferences.edit().putString(CommonAttributes.FIRMWARE_VERSION + "-" + macid, fwVer).apply();
        }

        @Override
        public void onGetDeviceAction(int type) throws RemoteException {
            showToast("onGetDeviceAction", "type:" + type);
            if(type == 5){
                if(bAutoEcg){
                    Intent intent = new Intent(MainActivity.this, EcgTestActivity.class);
                    intent.putExtra("auto", true);
                    startActivityForResult(intent, 0);
                    bAutoEcg = false;
                }
            }
        }


        @Override
        public void onGetBandFunction(int result, boolean[] results) throws RemoteException {
            showToast("onGetBandFunction", "result : " + result + ", results :" + results.length);

            String function = "";
            for (int i = 0; i < results.length; i++) {
                function += String.valueOf((i + 1) + "=" + results[i] + " ");
            }
            showToast("onGetBandFunction", function);
        }

        @Override
        public void onSetLanguage(int arg0) throws RemoteException {
            showToast("onSetLanguage", "result:" + arg0);

        }


        @Override
        public void onSendWeather(int arg0) throws RemoteException {
            showToast("onSendWeather", "result:" + arg0);
        }


        @Override
        public void onSetAntiLost(int arg0) throws RemoteException {
            showToast("onSetAntiLost", "result:" + arg0);

        }


        @Override
        public void onReceiveSensorData(int arg0, int arg1, int arg2, int arg3,
                                        int arg4) throws RemoteException {
            showToast("onReceiveSensorData", "result:" + arg0 + " , " + arg1 + " , " + arg2 + " , " + arg3 + " , " + arg4);
        }


        @Override
        public void onSetBloodPressureMode(int arg0) throws RemoteException {
            showToast("onSetBloodPressureMode", "result:" + arg0);
        }


        @Override
        public void onGetMultipleSportData(int flag, String recorddate, int mode, int value)
                throws RemoteException {
//            Date date = new Date(timestamp * 1000);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//            String recorddate = sdf.format(date);
            showToast("onGetMultipleSportData", "flag:" + flag + " , mode :" + mode + " recorddate:" + recorddate + " , value :" + value);
        }


        @Override
        public void onSetGoalStep(int result) throws RemoteException {
            showToast("onSetGoalStep", "result:" + result);
        }


        @Override
        public void onSetDeviceHeartRateArea(int result) throws RemoteException {
            showToast("onSetDeviceHeartRateArea", "result:" + result);
        }


        @Override
        public void onSensorStateChange(int type, int state)
                throws RemoteException {

            showToast("onSensorStateChange", "type:" + type + " , state : " + state);
        }

        @Override
        public void onReadCurrentSportData(int mode, String time, int step,
                                           int cal) throws RemoteException {

            showToast("onReadCurrentSportData", "mode:" + mode + " , time : " + time + " , step : " + step + " cal :" + cal);
        }

        @Override
        public void onGetOtaInfo(boolean isUpdate, String version, String path) throws RemoteException {
            showToast("onGetOtaInfo", "isUpdate " + isUpdate + " version " + version + " path " + path);
        }

        @Override
        public void onGetOtaUpdate(int step, int progress) throws RemoteException {
            showToast("onGetOtaUpdate", "step " + step + " progress " + progress);
        }

        @Override
        public void onSetDeviceCode(int result) throws RemoteException {
            showToast("onSetDeviceCode", "result " + result);
        }

        @Override
        public void onGetDeviceCode(byte[] bytes) throws RemoteException {
            showToast("onGetDeviceCode", "bytes " + SysUtils.printHexString(bytes));
        }

        @Override
        public void onCharacteristicChanged(String uuid, byte[] bytes) throws RemoteException {
            showToast("onCharacteristicChanged", uuid + " " + SysUtils.printHexString(bytes));
        }

        @Override
        public void onCharacteristicWrite(String uuid, byte[] bytes, int status) throws RemoteException {
            showToast("onCharacteristicWrite", status + " " + uuid + " " + SysUtils.printHexString(bytes));
        }

        @Override
        public void onSetEcgMode(int result, int state) throws RemoteException {
            showToast("onSetEcgMode", "result " + result + " state " + state);
        }

        @Override
        public void onGetEcgValue(int state, int[] values) throws RemoteException {
            showToast("onGetEcgValue", "state " + state + " value " + values.length);
            Intent intent = new Intent(SampleBleService.ECG_VALUE);
            intent.putExtra("values", values);
            sendBroadcast(intent);
        }

        @Override
        public void onGetEcgHistory(long timestamp, int number) throws RemoteException {
            showToast("onGetEcgHistory", "timestamp " + timestamp + " number " + number);

        }

        @Override
        public void onGetEcgStartEnd(int id, int state, long timestamp) throws RemoteException {
            showToast("onGetEcgStartEnd", "id " + id + " state " + state + " timestamp " + timestamp);

        }

        @Override
        public void onGetEcgHistoryData(int id, int[] values) throws RemoteException {
            showToast("onGetEcgHistoryData", "id " + id + " values " + values.length);

        }

        @Override
        public void onSetDeviceName(int result) throws RemoteException {
            showToast("onSetDeviceName", "result " + result);
        }

        @Override
        public void onGetDeviceRssi(int rssi) throws RemoteException {
            showToast("onGetDeviceRssi", "rssi " + rssi);
        }

        @Override
        public void onSetReminder(int result) throws RemoteException {
            showToast("onSetReminder", "result " + result);

        }

        @Override
        public void onSetReminderText(int result) throws RemoteException {
            showToast("onSetReminderText", "result " + result);

        }

        @Override
        public void onSetBPAdjust(int result) throws RemoteException {
            showToast("onSetBPAdjust", "result " + result);

        }

        @Override
        public void onSetTemperatureMode(int result) throws RemoteException {
            showToast("onSetTemperatureMode", "result " + result);

        }

        @Override
        public void onGetTemperatureData(int surfaceTemp,int bodyTemp) throws RemoteException {
            showToast("onGetTemperatureData", "surfaceTemp " + surfaceTemp + ", bodyTemp" + bodyTemp);

        }

        @Override
        public void onTemperatureModeChange(int enable) throws RemoteException {
            showToast("onTemperatureModeChange", "enable " + enable);
        }

        @Override
        public void onGetDeviceDial(String productType, String productId, int watchWidth, int watchHeight, int unitWidth, int colorMode, int isCustom, int dialId, int reviewWatchWidth, int reviewWatchHeight, int shapeType) throws RemoteException {
            showToast("onGetDeviceDial", productType + "," + productId + "," + watchWidth + "," + watchHeight + "," + unitWidth + "," + colorMode + "," + isCustom + "," + reviewWatchWidth + "," + reviewWatchHeight + "," + shapeType);

            sharedPreferences.edit().putString(CommonAttributes.FUNCTION_DIAL_PRODUCTTYPE + "-" + macid, productType).apply();
            sharedPreferences.edit().putString(CommonAttributes.FUNCTION_DIAL_PRODUCTID + "-" + macid, productId).apply();
            sharedPreferences.edit().putString(CommonAttributes.FUNCTION_DIAL_ID + "-" + macid, String.valueOf(dialId)).apply();

            deviceColorType = colorMode;
            deviceShapeType = shapeType;
            deviceWatchWidth = watchWidth;
            deviceWatchHeight = watchHeight;
            deviceWatchUnitWidth = unitWidth;
            deviceWatchReviewWidth = reviewWatchWidth;
            deviceWatchReviewHeight = reviewWatchHeight;
        }

        @Override
        public void onSetDeviceDialState() throws RemoteException {
            showToast("onSetDeviceDialState", "");
        }

        @Override
        public void onSetDeviceWallpaperState() throws RemoteException {
            showToast("onSetDeviceWallpaperState", "");
        }

        @Override
        public void onEditDeviceDialCustom() throws RemoteException {
            showToast("onEditDeviceDialCustom", "");
        }

        @Override
        public void onGetDeviceDialCustom(int timePos, int timeAboveContent, int timeBelowContent, int fontColorType) throws RemoteException {
            showToast("onGetDeviceDialCustom", timePos + "," + timeAboveContent + "," + timeBelowContent + "," + fontColorType);
        }

        @Override
        public void onSetFemaleReminder() throws RemoteException {
            showToast("onSetFemaleReminder", "");
        }

        @Override
        public void onNotifyClassicBtName(String deviceBtName) throws RemoteException {
            showToast("onNotifyClassicBtName", deviceBtName);
            mClassicBtDeviceBtName = deviceBtName;
        }

        @Override
        public void onNotifyClassicBtInfo(int btState, int pareState, String deviceMac, String phoneMac) throws RemoteException {
            showToast("onNotifyClassicBtInfo", btState + "," + pareState + "," + deviceMac + "," + phoneMac);
            mClassicBtDeviceBtName = "";
            if(btState == 1) {
                // "00:11:22:33:AA:BB"

                boolean bBonded = false;
                //å¾—åˆ°æ‰€æœ‰å·²é…å¯¹çš„è“ç‰™é€‚é…å™¨å¯¹è±¡
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                if(devices.size() > 0) {
                    for (Iterator iterator = devices.iterator(); iterator.hasNext(); ) {
                        BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                        //å¾—åˆ°è¿œç¨‹å·²é…å¯¹è“ç‰™è®¾å¤‡çš„macåœ°å€
                        saveLog("onNotifyClassicBtInfo : " + bluetoothDevice.getAddress() + " type: " + bluetoothDevice.getType());
                        if(bluetoothDevice.getAddress().equalsIgnoreCase(deviceMac)) {
                            bBonded = true;
                            saveLog("device bonded already");
                        }
                    }
                }

                if(pareState != 1) {
                    String regex = "(.{2})";
                    deviceMac = deviceMac.replaceAll(regex, "$1:");
                    deviceMac = deviceMac.substring(0, deviceMac.length() - 1);
                    saveLog("device mac: " + deviceMac);
                    BluetoothDevice bd = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceMac);

                    Intent intent = new Intent(CommonAttributes.ACTION_NOTIFY_CLASSIC_BT_CREATE_BOND);
                    intent.putExtra("device", bd);
                    sendBroadcastWithPackage(intent);
                }
            }
        }

        @Override
        public void onNotifyContactCrc(String contactCrc) throws RemoteException {
            showToast("onNotifyContactCrc", contactCrc);
        }

        @Override
        public void onNotifyAppId(String appId) throws RemoteException {
            showToast("onNotifyAppId", appId);
        }

        @Override
        public void onGetPhoneVolume() throws RemoteException {
            showToast("onGetPhoneVolume", "");
        }

        @Override
        public void onNotifyBindedInfo(int action, int state) throws RemoteException {
            showToast("onNotifyBindedInfo", String.valueOf(action) + ", " + String.valueOf(state));

            switch(action) {
                case CommonAttributes.BOND_ACTION_INIT:
                    if(state == CommonAttributes.BOND_STATE_NO) {
                        try {
                            mService.setBindedInfo(CommonAttributes.BOND_ACTION_APP_START, CommonAttributes.BOND_STATE_NO, CommonAttributes.OS_TYPE);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(state == CommonAttributes.BOND_STATE_YES) {
                    }
                    break;
                case CommonAttributes.BOND_ACTION_ACK:
                    try {
                        mService.setBindedInfo(CommonAttributes.BOND_ACTION_SUCCESS, CommonAttributes.BOND_STATE_NO, CommonAttributes.OS_TYPE);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case CommonAttributes.BOND_ACTION_ACK_CANCEL:
                    saveLog("onNotifyBindedInfo : disconnect device");
                    // update sync state

                    Intent intent = new Intent(CommonAttributes.ACTION_NOTIFY_UNBOND_ACK);
                    sendBroadcastWithPackage(intent);
                    break;
                case CommonAttributes.BOND_ACTION_SUCCESS:
                    saveLog("onNotifyBindedInfo : bonded confirm");
                    break;
                case CommonAttributes.BOND_ACTION_UNBOND_ACK:
                    saveLog("onNotifyBindedInfo : unbond device ack");
                    // update sync state

                    intent = new Intent(CommonAttributes.ACTION_NOTIFY_UNBOND_ACK);
                    sendBroadcastWithPackage(intent);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onGetDeviceState(boolean bHandOfflight, boolean bVibrate, boolean bNoDisturb) throws RemoteException {
            showToast("onGetDeviceState", String.valueOf(bHandOfflight) + ", " + String.valueOf(bVibrate) + ", " + String.valueOf(bNoDisturb));
        }

        @Override
        public void onNotifyECardNeedUpdate(byte[] bytes) throws RemoteException {
            ArrayList<ECardInfoItem> alItem = new ArrayList<>();
            ECardInfoItem item = new ECardInfoItem();
            item.setEcardId(1);
            item.setName("111");
            item.setContent("111...");
            alItem.add(item);

//            item = new ECardInfoItem();
//            item.setEcardId(2);
//            item.setName("222");
//            item.setContent("222...");
//            alItem.add(item);
//
//            item = new ECardInfoItem();
//            item.setEcardId(3);
//            item.setName("333");
//            item.setContent("333...");
//            alItem.add(item);
//
//            item = new ECardInfoItem();
//            item.setEcardId(4);
//            item.setName("444");
//            item.setContent("444...");
//            alItem.add(item);
//
//            item = new ECardInfoItem();
//            item.setEcardId(5);
//            item.setName("555");
//            item.setContent("555...");
//            alItem.add(item);
//
//            item = new ECardInfoItem();
//            item.setEcardId(6);
//            item.setName("666");
//            item.setContent("666...");
//            alItem.add(item);
//
//            item = new ECardInfoItem();
//            item.setEcardId(7);
//            item.setName("777");
//            item.setContent("777...");
//            alItem.add(item);
//
//            item = new ECardInfoItem();
//            item.setEcardId(8);
//            item.setName("888");
//            item.setContent("888...");
//            alItem.add(item);
//
//            item = new ECardInfoItem();
//            item.setEcardId(9);
//            item.setName("999");
//            item.setContent("999...");
//            alItem.add(item);
//
//            item = new ECardInfoItem();
//            item.setEcardId(10);
//            item.setName("aaa");
//            item.setContent("aaa...");
//            alItem.add(item);

            ECardInfo info = new ECardInfo(alItem);
            mService.setECardInfoContent(info);
        }

        @Override
        public void onNotifySmsRspNeedUpdate(byte[] bytes) throws RemoteException {
            ArrayList<SmsRspInfoItem> alItem = new ArrayList<>();
            SmsRspInfoItem item = new SmsRspInfoItem();
            item.setSmsRspId(1);
            item.setContent("111");
            alItem.add(item);

            item = new SmsRspInfoItem();
            item.setSmsRspId(2);
            item.setContent("222");
            alItem.add(item);

            item = new SmsRspInfoItem();
            item.setSmsRspId(3);
            item.setContent("333");
            alItem.add(item);

            SmsRspInfo info = new SmsRspInfo(alItem);
            mService.setSmsRspInfoContent(info);
        }

        @Override
        public void onNotifySmsRspSend(int id, String phoneNum) throws RemoteException {
            //TODO
            saveLog("onNotifySmsRspSend id = " + id + ", phoneNum = " + phoneNum);
        }

        @Override
        public void onGetChatgptAction(int type) throws RemoteException {
            //TODO
            saveLog("onGetChatgptAction type = " + type);
        }

        @Override
        public void onGetFactoryTestData(byte[] bytes) throws RemoteException {
            saveLog("onGetFactoryTestData data = " + bytes.toString());
            Intent intent = new Intent(SampleBleService.FACTORYMODE_TEST_VALUE);
            intent.putExtra("values", bytes);
            sendBroadcast(intent);
        }

        @Override
        public void onNotifyDialJsonContent(String content) throws RemoteException {
            saveLog("onNotifyDialJsonContent content = " + content);
            Intent intent = new Intent(CommonAttributes.ACTION_NOTIFY_DIAL_JSON_CONTENT);
            intent.putExtra("content", content);
            sendBroadcast(intent);
        }

        @Override
        public void onGetSportSteps(int steps) throws RemoteException {

        }

    };
    private int sleepcount = 0;
    private boolean bStart = false;

    private PopupWindow window;

    private ArrayList<BleDeviceItem> nearbyItemList;

    private listDeviceViewAdapter nearbyListAdapter;

    private Button btBind;
    private Button btUnbind;
    private Button btScan;
    private Button btConnect;
    private Button btDisconnect;
    //	private Button btReadCurSteps;
    private Button btReadFw;
    private TextView tvSync;
    private Button btSyncPersonalInfo;
    private Button bNotify;
    private Button set_time, getcursportdata, set_parameters;
    private TextView data_text;
    private Button set_userinfo, set_vir, set_photo, set_idletime, set_sleep, read_batery, read_fw, set_alarm, btOtaFirmware, set_autoheart, set_fuzhu, set_showmode, openheart, closeheart, getdata;
    private EditText et_getdata, et_getday;
    private Button setLanguage, send_weather, bt_getmutipleSportData, bt_open_blood, bt_close_blood, bt_setgoalstep, bt_setHeartRateArea;
    private ScrollView svDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShareUtil.init(getApplicationContext());
        sharedPreferences = getSharedPreferences("jy_sdk_prefs", Context.MODE_PRIVATE);
        initView();
        llConnect.setVisibility(View.GONE);

        demo();

        Intent gattServiceIntent = new Intent(this,
                SampleBleService.class);
//        gattServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cName = startService(gattServiceIntent);
        // å¯åŠ¨è“ç‰™æœåŠ¡
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SampleBleService.ECG_SWITCH);
        intentFilter.addAction(SampleBleService.FACTORYMODE_TEST_SWITCH);
        intentFilter.addAction(CommonAttributes.ACTION_NOTIFY_CLASSIC_BT_NEED_CONNECT);
        intentFilter.addAction(CommonAttributes.ACTION_NOTIFY_CLASSIC_BT_RETRY_BOND);
        intentFilter.addAction(CommonAttributes.ACTION_NOTIFY_CLASSIC_BT_CREATE_BOND);
        intentFilter.addAction(CommonAttributes.ACTION_REQUEST_DIAL_JSON_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(broadcastReceiver, intentFilter);
        }

        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        requestmanageexternalstorage_Permission();
        ActivityCompat.requestPermissions(this, permissions, 0);

        CheckUtil.checkPermission(this, "", Manifest.permission.READ_EXTERNAL_STORAGE, 0);
        // å¼€å¯è“ç‰™
        CheckUtil.checkBluetooth(this, getString(R.string.state_ble_stack_error));

        selectorStyle = new PictureSelectorStyle();
        launcherResult = createActivityResultLauncher();
    }

    /**
     * åˆ›å»ºä¸€ä¸ªActivityResultLauncher
     *
     * @return
     */
    private ActivityResultLauncher<Intent> createActivityResultLauncher() {
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        int resultCode = result.getResultCode();
                        if (resultCode == RESULT_OK) {
                            ArrayList<LocalMedia> selectList = PictureSelector.obtainSelectorList(result.getData());
                            analyticalSelectResults(selectList);
                        } else if (resultCode == RESULT_CANCELED) {
                            saveLog("onActivityResult PictureSelector Cancel");
                        }
                    }
                });
    }

    /**
     * å¤„ç†é€‰æ‹©ç»“æžœ
     *
     * @param result
     */
    private void analyticalSelectResults(ArrayList<LocalMedia> result) {
        for (LocalMedia media : result) {
            if (media.getWidth() == 0 || media.getHeight() == 0) {
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    MediaExtraInfo imageExtraInfo = MediaUtils.getImageSize(this, media.getPath());
                    media.setWidth(imageExtraInfo.getWidth());
                    media.setHeight(imageExtraInfo.getHeight());
                } else if (PictureMimeType.isHasVideo(media.getMimeType())) {
                    MediaExtraInfo videoExtraInfo = MediaUtils.getVideoSize(this, media.getPath());
                    media.setWidth(videoExtraInfo.getWidth());
                    media.setHeight(videoExtraInfo.getHeight());
                }
            }
            saveLog("æ–‡ä»¶å: " + media.getFileName());
            saveLog("æ˜¯å¦åŽ‹ç¼©:" + media.isCompressed());
            saveLog("åŽ‹ç¼©:" + media.getCompressPath());
            saveLog("åˆå§‹è·¯å¾„:" + media.getPath());
            saveLog("ç»å¯¹è·¯å¾„:" + media.getRealPath());
            saveLog("æ˜¯å¦è£å‰ª:" + media.isCut());
            saveLog("è£å‰ªè·¯å¾„:" + media.getCutPath());
            saveLog("æ˜¯å¦å¼€å¯åŽŸå›¾:" + media.isOriginal());
            saveLog("åŽŸå›¾è·¯å¾„:" + media.getOriginalPath());
            saveLog("æ²™ç›’è·¯å¾„:" + media.getSandboxPath());
            saveLog("æ°´å°è·¯å¾„:" + media.getWatermarkPath());
            saveLog("è§†é¢‘ç¼©ç•¥å›¾:" + media.getVideoThumbnailPath());
            saveLog("åŽŸå§‹å®½é«˜: " + media.getWidth() + "x" + media.getHeight());
            saveLog("è£å‰ªå®½é«˜: " + media.getCropImageWidth() + "x" + media.getCropImageHeight());
            saveLog("æ–‡ä»¶å¤§å°: " + PictureFileUtils.formatAccurateUnitFileSize(media.getSize()));
            saveLog("æ–‡ä»¶æ—¶é•¿: " + media.getDuration());

            String newFilename = media.getPath();
            saveLog("select result getPath:" + newFilename);
            newFilename = media.getCutPath();
            saveLog("select result getCutPath:" + newFilename);
            //imagePaths.add(selectList.get(i).getPath());
            // è¯»å–å›¾ç‰‡å¤§å°, æ£€æµ‹æœ€å°è¾¹æ˜¯å¦å°äºŽæ‰‹çŽ¯æœ€å°è¾¹
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;//è¿™ä¸ªå‚æ•°è®¾ç½®ä¸ºtrueæ‰æœ‰æ•ˆï¼Œ
            Bitmap bmp = BitmapFactory.decodeFile(newFilename, options);//è¿™é‡Œçš„bitmapæ˜¯ä¸ªç©º
            if (bmp == null) {
                saveLog("é€šè¿‡optionsèŽ·å–åˆ°çš„bitmapä¸ºç©º ===");
            }
            int outHeight = options.outHeight;
            int outWidth = options.outWidth;
            if (outWidth < deviceWatchWidth || outHeight < deviceWatchHeight) {
                saveLog("analyticalSelectResults out size :" + outWidth + ", " + outHeight + " | watch size: " + deviceWatchWidth + ", " + deviceWatchWidth);
                Toast.makeText(MainActivity.this, getString(R.string.Image_Frame_Error), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String outFilePath = "/wallpaper";
                String outFileName = "wallpaper.bin";

                int timePos = 0;            // time label display position. 0: right bottom, 1: right top, 2: left bottom, 3: left top
                int timeAboveContent = 0;   // above time label content. 0: empty, 1: date, 2: sleep, 3:heart rate, 4: steps
                int timeBelowContent = 0;   // under time label content. 0: empty, 1: date, 2: sleep, 3:heart rate, 4: steps
                int fontRed = 0;            // font color rgb(red)
                int fontGreen = 0;          // font color rgb(green)
                int fontBlue = 0;           // font color rgb(blue)

                // image file width and height should match the deviceWatchWidth and deviceWatchHeight
                String wallpaperFielname = mService.translateBmpToBin(newFilename, outFilePath, outFileName,
                                                                        deviceColorType, deviceShapeType, deviceWatchWidth, deviceWatchHeight,
                                                                        deviceWatchReviewWidth, deviceWatchReviewHeight, deviceWatchUnitWidth,
                                                                        timePos, timeAboveContent, timeBelowContent, fontRed, fontGreen, fontBlue);
                mService.startFileOta(ota_mode_wallpaper, wallpaperFielname);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestmanageexternalstorage_Permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // å…ˆåˆ¤æ–­æœ‰æ²¡æœ‰æƒé™
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Android VERSION  R OR ABOVEï¼ŒHAVE MANAGE_EXTERNAL_STORAGE GRANTED!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Android VERSION  R OR ABOVEï¼ŒNO MANAGE_EXTERNAL_STORAGE GRANTED!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, 11);
            }
        }
    }

    private void demo() {
        int mode = 0;
        int value_ext1 = 0x00;
        int value_ext2 = 0x11;
        int value_ext3 = 0x10;

        int value1 = 0x00;
        mode = (value_ext1 & 0xF0) | value1 & 0x0F;
        value1 = 0x00;
        mode = ((value_ext1 & 0x0F) << 4) | value1 & 0x0F;

        value1 = 0x69;
        mode = (value_ext2 & 0xF0) | value1 & 0x0F;
        value1 = 0x89;
        mode = ((value_ext2 & 0x0F) << 4) | value1 & 0x0F;

        value1 = 0x79;
        mode = (value_ext3 & 0xF0) | value1 & 0x0F;
        value1 = 0x00;
        mode = ((value_ext3 & 0x0F) << 4) | value1 & 0x0F;
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            if(action.equalsIgnoreCase(SampleBleService.ECG_SWITCH)) {
                boolean state = intent.getBooleanExtra("state", false);
                int mode = intent.getIntExtra("mode", 0);
                try {
                    mService.setEcgMode(state, mode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else if(action.equalsIgnoreCase(SampleBleService.FACTORYMODE_TEST_SWITCH)) {
                boolean mode = intent.getBooleanExtra("mode", false);
                try {
                    mService.startFactoryTestMode(mode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else if(action.equalsIgnoreCase(CommonAttributes.ACTION_NOTIFY_CLASSIC_BT_NEED_CONNECT)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                                .setMessage(getString(R.string.msg_bt_pair_error) + mClassicBtDeviceBtName)
                                .setPositiveButton(R.string.action_bt_setting, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                                        startActivity(intent);

                                    }
                                })
                                .setNegativeButton(R.string.action_cancel, null).create();
                        alertDialog.show();
                    }
                }, 1000);
            }
            else if(action.equalsIgnoreCase(CommonAttributes.ACTION_NOTIFY_CLASSIC_BT_RETRY_BOND)) {
                // retry 10 times 100 ms delay
                String deviceMac = intent.getStringExtra("deviceMac");
                BluetoothDevice bd = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceMac);
                tryBondDevice(bd, CommonAttributes.MAX_RETRY_BOND_TIMES);
            }
            else if(action.equalsIgnoreCase(CommonAttributes.ACTION_NOTIFY_CLASSIC_BT_CREATE_BOND)) {
                BluetoothDevice bdBond = intent.getParcelableExtra("device");
                boolean bResultBond = SysUtils.boundDeviceAPI(bdBond);
                saveLog("create bond device: " + bResultBond);
            }
            else if(action.equalsIgnoreCase(CommonAttributes.ACTION_REQUEST_DIAL_JSON_CONTENT)) {
                String fw = intent.getStringExtra("fw");
                try {
                    saveLog("request dial json content: " + fw);
                    mService.getDialServerInfo(fw);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void tryBondDevice(BluetoothDevice bd, final int leftTimes) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean bResult = SysUtils.boundDeviceAPI(bd);
                saveLog("create bond device " + leftTimes + " : " + bResult);
                if(leftTimes > 0) {
                    int times = leftTimes - 1;
                    tryBondDevice(bd, times);
                }
            }
        }, 100);
    }

    private ScrollView svLog;

    private void initView() {
        svLog = (ScrollView) findViewById(R.id.svLog);

        llConnect = (LinearLayout) findViewById(R.id.llConnect);
        data_text = (TextView) findViewById(R.id.data_text);
        data_text.setOnClickListener(this);
        btBind = (Button) findViewById(R.id.bind);
        btUnbind = (Button) findViewById(R.id.unbind);
        btScan = (Button) findViewById(R.id.scan);
        btConnect = (Button) findViewById(R.id.connect);
        btDisconnect = (Button) findViewById(R.id.disconnect);
        btReadFw = (Button) findViewById(R.id.read_fw);
        btSyncPersonalInfo = (Button) findViewById(R.id.set_alarm);
        set_time = (Button) findViewById(R.id.set_time);
        getcursportdata = (Button) findViewById(R.id.getcursportdata);
        set_parameters = (Button) findViewById(R.id.set_parameters);
        tvSync = (TextView) findViewById(R.id.tvSync);
        bNotify = (Button) findViewById(R.id.bNotify);

        set_userinfo = (Button) findViewById(R.id.set_userinfo);
        set_userinfo.setOnClickListener(this);
        set_vir = (Button) findViewById(R.id.set_vir);
        set_vir.setOnClickListener(this);
        set_photo = (Button) findViewById(R.id.set_photo);
        set_photo.setOnClickListener(this);
        set_idletime = (Button) findViewById(R.id.set_idletime);
        set_idletime.setOnClickListener(this);
        set_sleep = (Button) findViewById(R.id.set_sleep);
        set_sleep.setOnClickListener(this);
        read_batery = (Button) findViewById(R.id.read_batery);
        read_batery.setOnClickListener(this);
        read_fw = (Button) findViewById(R.id.read_fw);
        read_fw.setOnClickListener(this);
        set_alarm = (Button) findViewById(R.id.set_alarm);
        set_alarm.setOnClickListener(this);
        btOtaFirmware = (Button) findViewById(R.id.btOtaFirmware);
        btOtaFirmware.setOnClickListener(this);
        set_autoheart = (Button) findViewById(R.id.set_autoheart);
        set_autoheart.setOnClickListener(this);
        set_fuzhu = (Button) findViewById(R.id.set_fuzhu);
        set_fuzhu.setOnClickListener(this);
        set_showmode = (Button) findViewById(R.id.set_showmode);
        set_showmode.setOnClickListener(this);
        openheart = (Button) findViewById(R.id.openheart);
        openheart.setOnClickListener(this);
        closeheart = (Button) findViewById(R.id.closeheart);
        closeheart.setOnClickListener(this);
        et_getdata = (EditText) findViewById(R.id.et_getdata);
        et_getday = (EditText) findViewById(R.id.et_getday);
        getdata = (Button) findViewById(R.id.getdata);
        getdata.setOnClickListener(this);
        setLanguage = (Button) findViewById(R.id.setLanguage);
        setLanguage.setOnClickListener(this);
        send_weather = (Button) findViewById(R.id.send_weather);
        send_weather.setOnClickListener(this);
        bt_getmutipleSportData = (Button) findViewById(R.id.bt_getmutipleSportData);
        bt_getmutipleSportData.setOnClickListener(this);
        bt_open_blood = (Button) findViewById(R.id.bt_open_blood);
        bt_open_blood.setOnClickListener(this);
        bt_close_blood = (Button) findViewById(R.id.bt_close_blood);
        bt_close_blood.setOnClickListener(this);
        bt_setgoalstep = (Button) findViewById(R.id.bt_setgoalstep);
        bt_setgoalstep.setOnClickListener(this);
        bt_setHeartRateArea = (Button) findViewById(R.id.bt_setHeartRateArea);
        bt_setHeartRateArea.setOnClickListener(this);

        bNotify.setOnClickListener(this);
        findViewById(R.id.bind).setOnClickListener(this);
        findViewById(R.id.unbind).setOnClickListener(this);
        findViewById(R.id.scan).setOnClickListener(this);
        findViewById(R.id.connect).setOnClickListener(this);
        findViewById(R.id.disconnect).setOnClickListener(this);
        findViewById(R.id.read_fw).setOnClickListener(this);
        findViewById(R.id.set_alarm).setOnClickListener(this);
        getcursportdata.setOnClickListener(this);
        set_time.setOnClickListener(this);
        set_parameters.setOnClickListener(this);

        svDevice = (ScrollView) findViewById(R.id.svDevice);
        findViewById(R.id.bGetDeviceCode).setOnClickListener(this);
        findViewById(R.id.bSetDeviceCode).setOnClickListener(this);
        findViewById(R.id.bGetBandFunction).setOnClickListener(this);
        findViewById(R.id.bSetUuid).setOnClickListener(this);
        findViewById(R.id.bTestEcg).setOnClickListener(this);
        findViewById(R.id.bEcgSync).setOnClickListener(this);

        findViewById(R.id.bSetName).setOnClickListener(this);
        findViewById(R.id.bGetRssi).setOnClickListener(this);
        findViewById(R.id.bSetReminderTime).setOnClickListener(this);
        findViewById(R.id.bSetReminderText).setOnClickListener(this);
        findViewById(R.id.bSetBPAdjust).setOnClickListener(this);
        findViewById(R.id.bSetSmsRsp).setOnClickListener(this);
        findViewById(R.id.bSetECard).setOnClickListener(this);
        findViewById(R.id.bSetContact).setOnClickListener(this);
        findViewById(R.id.bOtaDial).setOnClickListener(this);
        findViewById(R.id.bOtaWallpaper).setOnClickListener(this);
        findViewById(R.id.bGetDeviceDial).setOnClickListener(this);
        findViewById(R.id.set_factorytestmode).setOnClickListener(this);
        findViewById(R.id.bSetAppId).setOnClickListener(this);
    }

    Handler updateConnectStateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //super.handleMessage(msg);
            Bundle data = msg.getData();
            int state = data.getInt("state");

            if (state == 2 || bAuthSuccess) {
                btBind.setEnabled(false);
                btUnbind.setEnabled(true);
                btScan.setEnabled(true);
                btConnect.setEnabled(false);
                btDisconnect.setEnabled(true);
                llConnect.setVisibility(View.VISIBLE);
            } else {
                btBind.setEnabled(false);
                btUnbind.setEnabled(true);
                btScan.setEnabled(true);
                btConnect.setEnabled(false);
                btDisconnect.setEnabled(false);
                llConnect.setVisibility(View.GONE);
            }
            return true;
        }
    });

    protected void updateConnectState(int state) {

        Message msg = new Message();
        Bundle data = new Bundle();
        data.putInt("state", state);
        msg.setData(data);
        updateConnectStateHandler.sendMessage(msg);
    }

    protected void showToast(String title, String content) {
        String file = "demo.log";
        if (bSaveLog)
            saveLog(title + " -> " + content);
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("title", title);
        data.putString("content", content);
        msg.setData(data);
        messageHandler.sendMessage(msg);
    }

    private boolean bColor = false;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private Handler messageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            String title = data.getString("title");
            String content = data.getString("content");

            saveLog(title + ": " + content);
            switch (title) {
                default:
                    String text = "[" + sdf.format(new Date()) + "] " + title + "\n" + content;
                    data_text.setText(text);
                    text = "<font color='" + (bColor ? "#00" : "#82") + "'>" + text.replace("\n", "<br>") + "</font><br>";
                    tvSync.append(Html.fromHtml(text));
                    svLog.fullScroll(View.FOCUS_DOWN);
                    bColor = !bColor;
                    break;
            }
            return true;
        }
    });

    Handler scanDeviceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //super.handleMessage(msg);
            Bundle data = msg.getData();
            String result = data.getString("result");
            nearbyListAdapter.notifyDataSetChanged();
            return true;
        }
    });

    protected String macid;

    private void callRemoteScanDevice() {
        if (nearbyItemList != null)
            nearbyItemList.clear();

        if (mService != null) {
            try {
                popWindow(findViewById(R.id.scan), R.layout.popwindow_devicelist);
                bStart = !bStart;
                mService.scanDevice(bStart);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean callRemoteIsConnected() {
        boolean isConnected = false;
        if (mService != null) {
            try {
                isConnected = mService.isConnectBt();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }

        return isConnected;
    }

    private String callRemoteGetConnectedDevice() {
        String deviceMac = "";
        if (mService != null) {
            try {
                deviceMac = mService.getConnectedDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }

        return deviceMac;
    }

    private void callRemoteConnect(String name, String mac) {
        if (mac == null || mac.length() == 0) {
            Toast.makeText(this, "ble device mac address is not correctly!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mService != null) {
            try {
                macid = mac;
                mService.connectBt(name, mac);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteDisconnect() {

        if (mService != null) {
            try {
                mService.disconnectBt(true);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }


    private int callRemoteIsAuthrize() {
        int isAuthrize = 0;
        if (mService != null) {
            try {
                isAuthrize = mService.isAuthrize();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }

        return isAuthrize;
    }

    private int callRemoteSetOption(BleClientOption opt) {
        int result = 0;
        if (mService != null) {
            try {
                result = mService.setOption(opt);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }

        return result;
    }


    private int callRemoteSetUserInfo() {
        int result = 0;
        if (mService != null) {
            try {
                result = mService.setUserInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }

        return result;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Process.killProcess(Process.myPid());
//            finish();
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    protected void onPause() {
        saveLog("onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        saveLog("onStop");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        saveLog("onDestroy");
        unregisterReceiver(broadcastReceiver);
        try {
            mService.disconnectBt(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mIsBound) {
            unbindService(mServiceConnection);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.data_text) {
            svLog.setVisibility(svLog.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        } else if (id == R.id.bind) {
            Intent intent = new Intent(IRemoteService.class.getName());
            intent.setClassName("com.sxr.sdk.ble.keepfit.client", "com.sxr.sdk.ble.keepfit.client.SampleBleService");
            boolean bResult = bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            mIsBound = true;
        } else if (id == R.id.unbind) {
            if (mIsBound) {
                btBind.setEnabled(true);
                btUnbind.setEnabled(false);
                btScan.setEnabled(false);
                btConnect.setEnabled(false);
                btDisconnect.setEnabled(false);
//        			btReadCurSteps.setEnabled(false);
                btReadFw.setEnabled(false);

                btConnect.setText(R.string.connect);
                btDisconnect.setText(R.string.disconnect);
//    				btReadCurSteps.setText(R.string.read_cur_steps);

                btSyncPersonalInfo.setEnabled(false);
                getcursportdata.setEnabled(false);
                set_time.setEnabled(false);
                set_parameters.setEnabled(false);

                try {
                    mService.unregisterCallback(mServiceCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(mServiceConnection);
                mIsBound = false;
                bAuthSuccess = false;
            }
        } else if (id == R.id.scan) {
            // æ£€æŸ¥å®šä½
            if(!CheckUtil.checkGps(MainActivity.this, getString(R.string.permission_location)))
                return;
            if(!CheckUtil.checkPermission(MainActivity.this, getString(R.string.permission_location), Manifest.permission.ACCESS_COARSE_LOCATION, 0))
                return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(!CheckUtil.checkPermission(MainActivity.this, getString(R.string.permission_ble_scan), Manifest.permission.BLUETOOTH_SCAN, 0))
                    return;

                if(!CheckUtil.checkPermission(MainActivity.this, getString(R.string.permission_ble_connect), Manifest.permission.BLUETOOTH_CONNECT, 0))
                    return;
            }

            bScan = true;
            callRemoteScanDevice();
        } else if (id == R.id.disconnect) {
            callRemoteDisconnect();
            bAuthSuccess = false;
        } else if (id == R.id.bNotify) {
            callNotify();
        } else if (id == R.id.set_parameters) {
            callSetParameters();
        } else if (id == R.id.set_time) {
            callSetDeviceTime();
        } else if (id == R.id.set_userinfo) {
            UserProfile userProfile = new UserProfile(10000, 170, 60, 50, 0, 1, 24);
            BleClientOption opt = new BleClientOption(userProfile, null, null);
            int result = callRemoteSetOption(opt);
            callRemoteSetUserInfo();
        } else if (id == R.id.getcursportdata) {
            callgetCurSportData();
        } else if (id == R.id.set_vir) {
            callSet_vir();
        } else if (id == R.id.set_photo) {
            callRemoteSetphoto();
        } else if (id == R.id.set_idletime) {
            callRemoteSetIdletime();
        } else if (id == R.id.set_sleep) {
            callRemoteSetSleepTime();
        } else if (id == R.id.read_batery) {
            callRemoteGetDeviceBatery();
        } else if (id == R.id.read_fw) {
//                callRemoteGetDeviceInfo();
        } else if (id == R.id.set_alarm) {
            callSetAlarm();
        } else if (id == R.id.set_autoheart) {
            callRemoteSetAutoHeartMode(true);
        } else if (id == R.id.set_fuzhu) {
            DeviceProfile deviceProfile = new DeviceProfile(true, true, false, 18, 20, 00, 00, true);
            BleClientOption opt2 = new BleClientOption(null, deviceProfile, null);
            int result2 = callRemoteSetOption(opt2);
            callRemoteSetDeviceMode();
        } else if (id == R.id.set_showmode) {
            callRemoteSetHourFormat();
        } else if (id == R.id.openheart) {
            callRemoteSetHeartRateMode(true);
        } else if (id == R.id.closeheart) {
            callRemoteSetHeartRateMode(false);
        } else if (id == R.id.getdata) {
            int type = Integer.valueOf(et_getdata.getText().toString());
            int day = Integer.valueOf(et_getday.getText().toString());

            callRemoteGetData(type, day);
        } else if (id == R.id.setLanguage) {
            callRemoteSetLanguage();
        } else if (id == R.id.send_weather) {
//            	Weather weather = new Weather();
            ArrayList<Weather> lWeathers = new ArrayList<Weather>();
            Weather weather = new Weather((int) (System.currentTimeMillis() / 1000), 300, 400, 7, 28, 2, 0, 0, 0, -20); //æ—¶é—´ ç™½å¤©ã€æ™šä¸Šå¤©æ°” ã€æœ€ä½Žæœ€é«˜æ¸© ç©ºæ°”è´¨é‡ã€PM2.5 UV AQI å½“å‰æ¸©åº¦
            lWeathers.add(weather);

            BleClientOption opt3 = new BleClientOption(null, null, null, lWeathers);
            callRemoteSetOption(opt3);
            callRemoteSetWeather();
        } else if (id == R.id.bt_getmutipleSportData) {
            callRemoteGetMutipleData(2);
        } else if (id == R.id.bt_open_blood) {
            callRemoteOpenBlood(true);
        } else if (id == R.id.bt_close_blood) {
            callRemoteOpenBlood(false);
        } else if (id == R.id.bt_setgoalstep) {
            callRemoteSetGoalStep(50);
        } else if (id == R.id.bt_setHeartRateArea) {
            callRemoteSetHeartRateArea(true, 150, 80);
        } else if (id == R.id.bGetDeviceCode) {
            callGetDeviceCode();
        } else if (id == R.id.bSetDeviceCode) {
            final EditText editTextCode = new EditText(this);

            new AlertDialog.Builder(this)
                    .setView(editTextCode)
                    .setTitle(R.string.set_name)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = editTextCode.getText().toString();
                            byte[] bytes = name.getBytes();
                            callSetDeviceCode(bytes);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

        } else if (id == R.id.bGetBandFunction) {
            callRemoteGetFunction();
        } else if (id == R.id.bSetUuid) {
            setUuid();
        } else if (id == R.id.bEcgSync) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long time = calendar.getTime().getTime();
            saveLog("getTime " + time);
            int offset = TimeZone.getDefault().getRawOffset();
            saveLog("getRawOffset " + offset);
            long timestamp = (time - 0) / 1000;
            try {
                mService.getEcgHistory((int) timestamp);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Note: Original code had fallthrough to bTestEcg - preserving that behavior
            startActivityForResult(new Intent(this, EcgTestActivity.class), 0);
        } else if (id == R.id.bTestEcg) {
            startActivityForResult(new Intent(this, EcgTestActivity.class), 0);
        } else if (id == R.id.bSetName) {
            final EditText editTextName = new EditText(this);
            new AlertDialog.Builder(this)
                    .setView(editTextName)
                    .setTitle(R.string.set_name)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = editTextName.getText().toString();
                            setDeviceName(name);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else if (id == R.id.bGetRssi) {
            try {
                if(mService != null)
                    mService.getDeviceRssi();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.bSetReminderTime) {
            final EditText editTextId = new EditText(this);
            editTextId.setHint("id type");
            new AlertDialog.Builder(this)
                    .setView(editTextId)
                    .setTitle(R.string.set_name)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = editTextId.getText().toString();
                            int id = 1;
                            int type = 1;
                            if(name.contains(" ")){
                                id = Integer.parseInt(name.split(" ")[0]);
                                type = Integer.parseInt(name.split(" ")[1]);
                            }
                            try {
                                if(mService != null)
                                    mService.setReminder(60, 0, 0, 18, 0, id, type);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

        } else if (id == R.id.bSetReminderText) {
            final EditText editTextText = new EditText(this);
            editTextText.setHint("id text");
            new AlertDialog.Builder(this)
                    .setView(editTextText)
                    .setTitle(R.string.set_name)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = editTextText.getText().toString();
                            int id = 1;
                            if(name.contains(" ")){
                                id = Integer.parseInt(name.split(" ")[0]);
                                name = name.split(" ")[1];
                            }
                            try {
                                if(mService != null)
                                    mService.setReminderText(id, name);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

        } else if (id == R.id.bSetBPAdjust) {
            int nSBP = 120;
            int nDBP = 70;
            try {
                if(mService != null)
                    mService.setBPAdjust(nSBP, nDBP);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else if (id == R.id.bSetAppId) {
            String appId = SysUtils.getPhoneUniqueId(sharedPreferences);
            saveLog("appId : " + appId);
            try {
                if(mService != null)
                    mService.setAppId(appId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.bSetSmsRsp) {
            try {
//                    int initCrc = 0xffffffff;
//                    String name = "bcav";
//                    int firstCrc = SysUtils.calcCrc(initCrc, name);
//
//                    String content = "A6ICIxIiwKICAicGVyc29uTmFtZSIgOiAi5p2O5bCa5rO9IiwKTYyOTBjMTQ2MmYyNiIsCiAgInICAiYXBwSWQiIDogInd4MWNjMBob25lTnVtYmVyIiA6ICiMDFhODU5MWNlZjcyNGRkYjk2YWI2YmM5MTVAIxODY2NjAwNzU2NCIKfQ==ewogICJpZCIgOilMTE2NDkiLAogICJpZGVudGl0eU51bWJlciIgOiAiNDQwNjA2MjAxNzA1MjMwNTU2IiwKICAiaWRlbnRpdHlUeXBlIiver-2.0";
//                    int secondCrc = SysUtils.calcCrc(firstCrc, content);
//
//                    saveLog("CRC32 = " + Integer.toHexString(secondCrc));

                if(mService != null) {
                    ArrayList<SmsRspInfoItem> alContact = new ArrayList<>();
                    SmsRspInfoItem contact = new SmsRspInfoItem();
                    contact.setSmsRspId(1);
                    contact.setContent("111");
                    alContact.add(contact);

                    contact = new SmsRspInfoItem();
                    contact.setSmsRspId(2);
                    contact.setContent("222");
                    alContact.add(contact);

                    contact = new SmsRspInfoItem();
                    contact.setSmsRspId(3);
                    contact.setContent("333");
                    alContact.add(contact);

                    SmsRspInfo cInfo = new SmsRspInfo(alContact);
                    mService.setSmsRspInfoCrc(cInfo);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.bSetECard) {
            try {
                if(mService != null) {
                    ArrayList<ECardInfoItem> alItem = new ArrayList<>();
                    ECardInfoItem item = new ECardInfoItem();
                    item.setEcardId(1);
                    item.setName("111");
                    item.setContent("111...");
                    alItem.add(item);

//                        item = new ECardInfoItem();
//                        item.setEcardId(2);
//                        item.setName("222");
//                        item.setContent("222...");
//                        alItem.add(item);
//
//                        item = new ECardInfoItem();
//                        item.setEcardId(3);
//                        item.setName("333");
//                        item.setContent("333...");
//                        alItem.add(item);
//
//                        item = new ECardInfoItem();
//                        item.setEcardId(4);
//                        item.setName("444");
//                        item.setContent("444...");
//                        alItem.add(item);
//
//                        item = new ECardInfoItem();
//                        item.setEcardId(5);
//                        item.setName("555");
//                        item.setContent("555...");
//                        alItem.add(item);
//
//                        item = new ECardInfoItem();
//                        item.setEcardId(6);
//                        item.setName("666");
//                        item.setContent("666...");
//                        alItem.add(item);
//
//                        item = new ECardInfoItem();
//                        item.setEcardId(7);
//                        item.setName("777");
//                        item.setContent("777...");
//                        alItem.add(item);
//
//                        item = new ECardInfoItem();
//                        item.setEcardId(8);
//                        item.setName("888");
//                        item.setContent("888...");
//                        alItem.add(item);
//
//                        item = new ECardInfoItem();
//                        item.setEcardId(9);
//                        item.setName("999");
//                        item.setContent("999...");
//                        alItem.add(item);
//
//                        item = new ECardInfoItem();
//                        item.setEcardId(10);
//                        item.setName("aaa");
//                        item.setContent("aaa...");
//                        alItem.add(item);

                    ECardInfo info = new ECardInfo(alItem);
                    mService.setECardInfoCrc(info);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.bSetContact) {
            try {
                if(mService != null) {
                    long timestamp = System.currentTimeMillis();
                    String lastCrc = String.valueOf(timestamp / 1000);
                    mService.setContactCrc(lastCrc);

                    ArrayList<ContactInfoItem> arrList = new ArrayList<>();
                    ContactInfoItem item = new ContactInfoItem();
                    item.setContactId(1);
                    item.setContactName("Gabriel");
                    item.setPhoneNum("13925254527");
                    arrList.add(item);

                    ContactInfo cInfo = new ContactInfo(arrList);
                    mService.setContactInfo(cInfo);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.btOtaFirmware) {
//                callGetOtaInfo();
            if(mService != null) {
                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
                properties.root = new File("/sdcard");
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File("/sdcard");
                properties.extensions = new String[]{"bin"};
                FilePickerDialog dialog = new FilePickerDialog(MainActivity.this,properties);
                dialog.setTitle("Select a File");
                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        //files is the array of the paths of files selected by the Application User.
                        String otaFilePath = files[0];
                        try {
                            mService.startFileOta(ota_mode_firmware, otaFilePath);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show();
            }
        } else if (id == R.id.bOtaDial) {
            if(mService != null) {
                startActivityForResult(new Intent(this, DialMarketActivity.class), 100);
//                        DialogProperties properties = new DialogProperties();
//                        properties.selection_mode = DialogConfigs.SINGLE_MODE;
//                        properties.selection_type = DialogConfigs.FILE_SELECT;
//                        properties.root = new File("/sdcard");
//                        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
//                        properties.offset = new File("/sdcard");
//                        properties.extensions = new String[]{"bin"};
//                        FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
//                        dialog.setTitle("Select a File");
//                        dialog.setDialogSelectionListener(new DialogSelectionListener() {
//                            @Override
//                            public void onSelectedFilePaths(String[] files) {
//                                //files is the array of the paths of files selected by the Application User.
//                                String otaFilePath = files[0];
//                                try {
//                                    mService.startFileOta(ota_mode_dial, otaFilePath);
//                                } catch (RemoteException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                        dialog.show();
            }
        } else if (id == R.id.bGetDeviceDial) {
            try {
                if(mService != null)
                    mService.getDeviceDial();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else if (id == R.id.bOtaWallpaper) {
            if(mService != null) {
                if(deviceColorType == 0
                        && deviceShapeType == 0
                        && deviceWatchWidth == 0
                        && deviceWatchHeight == 0
                        && deviceWatchUnitWidth == 0
                        && deviceWatchReviewWidth == 0
                        && deviceWatchReviewHeight == 0) {
                    showToast("Warnning", "Call getDeviceDial() function first.");
                    return ;
                }
                int corpWidth = deviceWatchWidth;
                int corpHeight = deviceWatchHeight;
                // è¿›å…¥ç›¸å†Œ ä»¥ä¸‹æ˜¯ä¾‹å­ï¼šç”¨ä¸åˆ°çš„ api å¯ä»¥ä¸å†™
                PictureSelectionModel selectionModel = PictureSelector.create(MainActivity.this)
                        .openGallery(SelectMimeType.TYPE_IMAGE)//ç›¸å†Œ åª’ä½“ç±»åž‹ PictureMimeType.ofAll()ã€ofImage()ã€ofVideo()ã€ofAudio()
                        .setImageEngine(GlideEngine.createGlideEngine())// å¤–éƒ¨ä¼ å…¥å›¾ç‰‡åŠ è½½å¼•æ“Žï¼Œå¿…ä¼ é¡¹
                        .setCropEngine(getCropFileEngine(corpWidth, corpHeight))
                        .setMaxSelectNum(1)
                        .setSelectionMode(SelectModeConfig.SINGLE)
                        .isDirectReturnSingle(true)
                        .setOutputCameraDir("/JYouPro");// è‡ªå®šä¹‰ç›¸æœºè¾“å‡ºç›®å½•åªé’ˆå¯¹Android Qä»¥ä¸‹ç‰ˆæœ¬ï¼Œå…·ä½“å‚è€ƒDemo
                forSelectResult(selectionModel);
//                    dialog.setDialogSelectionListener(new DialogSelectionListener() {
//                        @Override
//                        public void onSelectedFilePaths(String[] files) {
//                            //files is the array of the paths of files selected by the Application User.
//                            String imgFullFileName = files[0];
//                            try {
//                                String outFilePath = "/wallpaper";
//                                String outFileName = "wallpaper.bin";
//
//                                int timePos = 0;            // time label display position. 0: right bottom, 1: right top, 2: left bottom, 3: left top
//                                int timeAboveContent = 0;   // above time label content. 0: empty, 1: date, 2: sleep, 3:heart rate, 4: steps
//                                int timeBelowContent = 0;   // under time label content. 0: empty, 1: date, 2: sleep, 3:heart rate, 4: steps
//                                int fontRed = 0;            // font color rgb(red)
//                                int fontGreen = 0;          // font color rgb(green)
//                                int fontBlue = 0;           // font color rgb(blue)
//
//                                // image file width and height should match the deviceWatchWidth and deviceWatchHeight
//                                String wallpaperFielname = mService.translateBmpToBin(imgFullFileName, outFilePath, outFileName,
//                                                                                        deviceColorType, deviceShapeType, deviceWatchWidth, deviceWatchHeight,
//                                                                                        deviceWatchReviewWidth, deviceWatchReviewHeight, deviceWatchUnitWidth,
//                                                                                        timePos, timeAboveContent, timeBelowContent, fontRed, fontGreen, fontBlue);
//                                mService.startFileOta(ota_mode_wallpaper, wallpaperFielname);
//                            } catch (RemoteException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    dialog.show();
            }
        } else if (id == R.id.set_factorytestmode) {
            startActivityForResult(new Intent(this, FactoryModeTestActivity.class), 0);
        }
    }

    private ActivityResultLauncher<Intent> launcherResult;
    private void forSelectResult(PictureSelectionModel model) {
//		switch (resultMode) {
//			case ACTIVITY_RESULT:
//				model.forResult(PictureConfig.CHOOSE_REQUEST);
//				break;
//			case CALLBACK_RESULT:
//				model.forResult(new MeOnResultCallbackListener());
//				break;
//			default:
        model.forResult(launcherResult);
//				break;
//		}

    }

    private boolean bOpen = false;


    private void setUuid() {
        if (mService != null) {
            try {
                bOpen = !bOpen;
                mService.setUuid(new String[0], new String[0], bOpen);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setDeviceName(String name) {
        if (mService != null) {
            try {
                mService.setDeviceName(name);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callGetDeviceCode() {
        if (mService != null) {
            try {
                mService.getDeviceCode();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callSetDeviceCode(byte[] bytes) {
        if (mService != null) {
            try {
                showToast("callSetDeviceCode", SysUtils.printHexString(bytes));
                mService.setDeviceCode(bytes);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callSetParameters() {
        int result;
        if (mService != null) {
            try {
                DeviceProfile deviceProfile = new DeviceProfile(false, true, false, 1, 2, 00, 00, true);
                BleClientOption opt2 = new BleClientOption(null, deviceProfile, null);
                int result2 = callRemoteSetOption(opt2);
                result = mService.setDeviceInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callSetAlarm() {
        boolean result;
        if (mService != null) {
            try {
                ArrayList<AlarmInfoItem> lAlarmInfo = new ArrayList<AlarmInfoItem>();
                AlarmInfoItem item = new AlarmInfoItem(5, 1, 23, 42, 1, 1, 1, 1, 1, 1, 1, "è¦ç¡è§‰æ³•å¤§å¸ˆå‚…å»ºç«‹å…¬å¸å…¬å¼€ç›‘æŽ§æœºæˆ¿çš„èŒƒå¾·è¨èŒƒå¾·è¨le", false);
                lAlarmInfo.add(item);
                BleClientOption bco = new BleClientOption(null, null, lAlarmInfo);
                mService.setOption(bco);
                mService.setAlarm();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callNotify() {
        boolean result;
        if (mService != null) {
            try {
                String type = ((EditText) findViewById(R.id.etType)).getText().toString();
                String name = ((EditText) findViewById(R.id.etName)).getText().toString();
                String content = ((EditText) findViewById(R.id.etContent)).getText().toString();
                result = mService.setNotify(System.currentTimeMillis() + "", Integer.parseInt(type), name, content);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callGetOtaInfo() {
        int result;
        if (mService != null) {
            try {
                result = mService.getOtaInfo(true);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callSetDeviceTime() {
        int result;
        if (mService != null) {
            try {
                result = mService.setDeviceTime();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callgetCurSportData() {
        int result;
        if (mService != null) {
            try {
                result = mService.getCurSportData();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callSet_vir() {
        int result;
        if (mService != null) {
            try {
                result = mService.sendVibrationSignal(4); //éœ‡åŠ¨4æ¬¡
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetphoto() {
        int result;
        if (mService != null) {
            try {
                result = mService.setPhontMode(true);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetIdletime() {
        int result;
        if (mService != null) {
            try {
                result = mService.setIdleTime(300, 14, 00, 18, 00);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetSleepTime() {
        int result;
        if (mService != null) {
            try {
                result = mService.setSleepTime(12, 00, 14, 00, 22, 00, 8, 00);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteGetDeviceBatery() {
        int result;
        if (mService != null) {
            try {
                result = mService.getDeviceBatery();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteGetDeviceInfo() {
        int result;
        if (mService != null) {
            try {
                result = mService.getDeviceInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetDeviceMode() {
        int result;
        if (mService != null) {
            try {
                result = mService.setDeviceMode(3);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetHourFormat() {
        int result;
        if (mService != null) {
            try {
                boolean is24HourFormat = DateFormat.is24HourFormat(this);
                result = mService.setHourFormat(is24HourFormat == true ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetHeartRateMode(boolean enable) {
        int result;
        if (mService != null) {
            try {
                result = mService.setHeartRateMode(enable, 60, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetAutoHeartMode(boolean enable) {
        int result;
        if (mService != null) {
            try {
                result = mService.setAutoHeartMode(enable, 18, 00, 19, 00, 15, 2); //18:00 - 19:00  15min 2min
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteGetData(int type, int day) {
        saveLog("callRemoteGetData");
        int result;
        if (mService != null) {
            try {
                result = mService.getDataByDay(type, day);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteGetFunction() {
        saveLog("callRemoteGetFunction");
        int result;
        if (mService != null) {
            try {
                result = mService.getBandFunction();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetLanguage() {
        saveLog("callRemoteGetData");
        int result;
        if (mService != null) {
            try {
                result = mService.setLanguage();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetWeather() {
        saveLog("callRemoteGetData");
        int result;
        if (mService != null) {
            try {
                result = mService.sendWeather();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteGetMutipleData(int day) {
        saveLog("callRemoteGetMutipleData");
        int result;
        if (mService != null) {
            try {
                result = mService.getMultipleSportData(day);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteOpenBlood(boolean enable) {
        saveLog("callRemoteGetMutipleData");
        int result;
        if (mService != null) {
            try {
                result = mService.setBloodPressureMode(enable);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetGoalStep(int step) {
        saveLog("callRemoteOpenBlood");
        int result;
        if (mService != null) {
            try {
                result = mService.setGoalStep(step);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callRemoteSetHeartRateArea(boolean enable, int max, int min) {
        saveLog("callRemoteOpenBlood");
        int result;
        if (mService != null) {
            try {
                result = mService.setDeviceHeartRateArea(enable, max, min);
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean dismissPopWindow() {
        if (window != null) {
            window.dismiss();
            window = null;

            return true;
        }

        return false;
    }

    private ListView nearbyListView;
    private boolean bScan = false;
    public void popWindow(View parent, int windowRes) {
        if (window == null) {
            LayoutInflater lay = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popView = lay.inflate(windowRes, null);

            nearbyItemList = new ArrayList<BleDeviceItem>();

            nearbyListView = (ListView) popView.findViewById(R.id.nearby_device_listView);

            nearbyListAdapter = new listDeviceViewAdapter(this, nearbyItemList);
            nearbyListAdapter.setType(listDeviceViewAdapter.DEVICE_NEARBY);
            nearbyListView.setAdapter(nearbyListAdapter);

            nearbyListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    bScan = false;
                    try {
                        ViewHolder holder = (ViewHolder) view.getTag();
                        callRemoteScanDevice();
                        callRemoteDisconnect();
                        macid = holder.mac;
                        ShareUtil.setValue("deviceName", holder.name);
                        sharedPreferences.edit().putString(CommonAttributes.P_MAC_ID, macid).apply();

                        callRemoteConnect(holder.name, holder.mac);
                        dismissPopWindow();
                    } catch (Exception e) {
                        e.printStackTrace();
                        saveLog("ble connect ble device: excption");
                    }
                }
            });

            popView.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    window.dismiss();
                    window = null;
                    return false;
                }

            });

            window = new PopupWindow(popView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
            window.setOutsideTouchable(true);
            window.setFocusable(true);
            window.update();
            window.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    bStart = true;
                    callRemoteScanDevice();
                    window = null;
                }
            });
        }
    }

    private class ViewHolder {
        TextView tvName;
        TextView address;
        TextView rssi;
        String name;
        String mac;
    }

    class listDeviceViewAdapter extends BaseAdapter implements
            OnItemSelectedListener {

        private static final int DEVICE_NEARBY = 0;
        int count = 0;
        private LayoutInflater layoutInflater;
        Context local_context;
        float xDown = 0, yDown = 0, xUp = 0, yUp = 0;
//        private List<BleDeviceItem> itemList;
        private int type;
        protected AnimationDrawable adCallBand;

        public listDeviceViewAdapter(Context context, List<BleDeviceItem> list) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //layoutInflater = LayoutInflater.from(context);    
            local_context = context;
//            itemList = list;
        }

        public int getCount() {
            return nearbyItemList.size();
        }

        public Object getItem(int pos) {
            return pos;
        }

        public long getItemId(int pos) {
            return pos;
        }

        public View getView(int pos, View v, ViewGroup p) {
            View view;
            ViewHolder viewHolder;

            BleDeviceItem item = nearbyItemList.get(pos);

            view = layoutInflater.inflate(R.layout.device_listitem_text, null);
            viewHolder = new ViewHolder();

            view.setTag(viewHolder);
            viewHolder.tvName = (TextView) view.findViewById(R.id.ItemTitle);
            viewHolder.address = (TextView) view.findViewById(R.id.ItemDate);
            viewHolder.rssi = (TextView) view.findViewById(R.id.ItemRssi);

            viewHolder.tvName.setText(item.getBleDeviceName());
            viewHolder.address.setText(item.getBleDeviceAddress());
            int rssi = item.getRssi();
            viewHolder.rssi.setText(String.valueOf(rssi));
            viewHolder.name = item.getBleDeviceName();
            viewHolder.mac = item.getBleDeviceAddress();

            return view;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position,
                                   long id) {
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

    }

    private void downloadFirmware(String url){
        saveLog("downloadFirmware url: " + url);
        String path = SysUtils.createSDCardDir(getApplicationContext(), CommonAttributes.P_FIRMWARE_PATH);
        if (path.length() != 0) {
            String firmwareFilename = path + "/" + CommonAttributes.P_WATCHFACE_bin;
            Date now = new Date();
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(url + "?t=" + String.valueOf(now.getTime()), new FileAsyncHttpResponseHandler(/* Context */ this) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, java.io.File file) {
                    saveLog("downloadFirmware onFailure");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, java.io.File file) {
                    try {
                        saveLog("downloadFirmware onSuccess");
                        FileInputStream is = new FileInputStream(file);
                        java.io.File fileDest = new java.io.File(firmwareFilename);
                        fileDest.getParentFile().mkdirs();
                        FileOutputStream fileout = new FileOutputStream(fileDest);
                        int cache = 10 * 1024;
                        byte[] buffer=new byte[cache];
                        int ch = 0;
                        while ((ch = is.read(buffer)) != -1) {
                            fileout.write(buffer,0,ch);
                        }
                        is.close();
                        fileout.flush();
                        fileout.close();

                        try {
                            mService.startFileOta(ota_mode_dial, firmwareFilename);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        saveLog(e.toString());
                        return ;
                    }
                    saveLog("downloadFirmware completed");
//                    Intent intent = new Intent(CommonAttributes.ACTION_NOTIFY_SERVER_FIRMWARE_DOWNLOAD_START);
//                    intent.putExtra("code", String.valueOf(statusCode));
//                    sendBroadcastWithPackage(intent);
                }

            });
        }
    }

    private boolean bAutoEcg = false;
    private String sAutoName = "";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 0: {
                    bAutoEcg = true;
                    sAutoName = (String) ShareUtil.getValue("etName", "");
                    findViewById(R.id.scan).performClick();
                    scanDeviceHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nearbyListView.performItemClick(nearbyListView.getChildAt(0), 0, 0);
                        }
                    }, 5000);
                    break;
                }
                case 100: {
                    String domain = data.getStringExtra("domain");
                    String filename = domain + data.getStringExtra("filename");
                    Date now = new Date();
                    String url = filename + "?t=" + String.valueOf(now.getTime());
                    saveLog("start download url: " + url);
                    downloadFirmware(url);
                    break;
                }
            }
        }
    }

    public void sendBroadcastWithPackage(Intent intent) {
        intent.setPackage(AppMng.PROCESS_NAME);
        sendBroadcast(intent);
    }

    public void saveLog(String content){
        Log.d(TAG, content);
        if(CheckUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            SysUtils.writeTxtToFile(this, content, CommonAttributes.P_LOG_PATH, SysUtils.getDateString(new Date(), 0, 2) + "-demo.log");
    }

}

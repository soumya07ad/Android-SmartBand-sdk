package com.sxr.sdk.ble.keepfit.client.factory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sxr.sdk.ble.keepfit.client.CommonAttributes;
import com.sxr.sdk.ble.keepfit.client.R;
import com.sxr.sdk.ble.keepfit.client.SampleBleService;
import com.sxr.sdk.ble.keepfit.client.SysUtils;
import com.sxr.sdk.ble.keepfit.client.ecg.ShareUtil;

import java.util.ArrayList;

public class FactoryModeTestActivity extends Activity {
    private String TAG = getClass().getSimpleName();
    private EditText etReceiveContent;
    private TextView tvContent;
    private ScrollView scrollView;
    private Button bOpen;
    private Button bClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_mode_test);
        initView();

        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void initData() {
        IntentFilter intentFilter = new IntentFilter(SampleBleService.FACTORYMODE_TEST_VALUE);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initView() {
        bOpen = (Button) findViewById(R.id.bOpen);
        bClose = (Button) findViewById(R.id.bClose);
        etReceiveContent = (EditText) findViewById(R.id.etReceiveContent);
        tvContent = (TextView) findViewById(R.id.tv_content);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        String name = (String) ShareUtil.getValue(CommonAttributes.DEVICE_ADDRESS, "");
        etReceiveContent.setText(name);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null)
                return;
            if(action.equals(SampleBleService.FACTORYMODE_TEST_VALUE)){
                byte[] values = intent.getByteArrayExtra("values");
                tvContent.append(SysUtils.printHexString(values).trim() + "\n");
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.smoothScrollTo(0, tvContent.getBottom());
                    }
                });
            }
        }
    };

    public void openFactoryModeTest(View view) {
        bOpen.setEnabled(false);
        bClose.setEnabled(true);
        Intent intent = new Intent(SampleBleService.FACTORYMODE_TEST_SWITCH);
        intent.putExtra("mode", true);
        sendBroadcast(intent);
    }

    public void closeFactoryModeTest(View view) {
        bOpen.setEnabled(true);
        bClose.setEnabled(false);
        Intent intent = new Intent(SampleBleService.FACTORYMODE_TEST_SWITCH);
        intent.putExtra("mode", false);
        sendBroadcast(intent);
    }

}

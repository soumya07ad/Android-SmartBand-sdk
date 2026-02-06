package com.sxr.sdk.ble.keepfit.client.dialmarket;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.sxr.sdk.ble.keepfit.client.R;


public class DialogLoading extends AlertDialog {

    public DialogLoading(Context context) {
        super(context, R.style.Dialog);
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
    }

    @Override
    public void show() {
        super.show();
    }

}

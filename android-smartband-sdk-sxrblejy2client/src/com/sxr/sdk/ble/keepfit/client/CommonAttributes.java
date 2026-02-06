package com.sxr.sdk.ble.keepfit.client;

public class CommonAttributes {
    public static final String ACTION_PREFIX = "demo";
    public static final String FUNCTION_DIAL_PRODUCTTYPE = "FUNCTION_DIAL_PRODUCTTYPE";
    public static final String FUNCTION_DIAL_PRODUCTID = "FUNCTION_DIAL_PRODUCTID";
    public static final String FUNCTION_DIAL_ID = "FUNCTION_DIAL_ID";

    public static final String FUNCTION_DIAL_PRODUCTTYPE_DEFAULT = "default";
    public static final String FUNCTION_DIAL_PRODUCTID_DEFAULT = "default";

    public static final String P_MAC_ID = "macid";
    public static final String P_MAC_ID_DEFAULT = "";

    public static final String FIRMWARE_VERSION = "FIRMWARE_VERSION";
    public static final String FIRMWARE_VERSION_DEFAULT = "";

    public static final String P_FIRMWARE_PATH = "/JYouProDemo/firmware";
    public static final String P_FIRMWARE_bin = "firmware.bin";
    public static final String P_WATCHFACE_bin = "watchface.bin";
    public static final String P_WALLPAPER_bin = "wallpaper.bin";
    public static final String P_LOG_PATH = "/JYouProDemo/log/";

    public static final String ACTION_NOTIFY_CLASSIC_BT_NEED_CONNECT = ACTION_PREFIX + "ACTION_NOTIFY_CLASSIC_BT_NEED_CONNECT";
    public static final String ACTION_NOTIFY_CLASSIC_BT_RETRY_BOND = ACTION_PREFIX + "ACTION_NOTIFY_CLASSIC_BT_RETRY_BOND";
    public static final String ACTION_NOTIFY_CLASSIC_BT_CREATE_BOND = ACTION_PREFIX + "ACTION_NOTIFY_CLASSIC_BT_CREATE_BOND";
    public static final int MAX_RETRY_BOND_TIMES = 10;

    public static final String ACTION_REQUEST_DIAL_JSON_CONTENT = ACTION_PREFIX + "ACTION_REQUEST_DIAL_JSON_CONTENT";
    public static final String ACTION_NOTIFY_DIAL_JSON_CONTENT = ACTION_PREFIX + "ACTION_NOTIFY_DIAL_JSON_CONTENT";

    public static final String APP_UNIQUEID = "APP_UNIQUEID";

    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static final int BOND_ACTION_INIT = 0;
    public static final int BOND_ACTION_APP_START = 1;
    public static final int BOND_ACTION_ACK = 2;
    public static final int BOND_ACTION_ACK_CANCEL = 3;
    public static final int BOND_ACTION_SUCCESS = 4;
    public static final int BOND_ACTION_UNBOND = 5;
    public static final int BOND_ACTION_UNBOND_ACK = 6;

    public static final int BOND_STATE_NO = 0;
    public static final int BOND_STATE_YES = 1;

    public static final int OS_TYPE = 1;

    public static final String ACTION_NOTIFY_UNBOND_ACK = ACTION_PREFIX + "ACTION_NOTIFY_UNBOND_ACK";
}

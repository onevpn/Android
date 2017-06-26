package co.onevpn.android.log;

import android.util.Log;

import co.onevpn.android.OneVpnApp;


public class Logger {
    private static final String TAG = "onevpn-app";
    public static void d(String tag, String message) {
        Log.d(tag, message);
    }

    public static void d(String message) {
        Log.d(TAG, message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }

    public static void w(int id) {
        Log.w(TAG, OneVpnApp.getInstance().getString(id));
    }
}

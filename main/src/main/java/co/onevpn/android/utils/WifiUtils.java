package co.onevpn.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.util.List;

public class WifiUtils {
    public static NetworkInfo getCurrentNetworkInfo(Context context) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return conn.getActiveNetworkInfo();
    }

    public static boolean isWifiContainsInList(Context context, List<String> wifiList) {
        WifiManager wifiService = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiService == null || wifiService.getConnectionInfo() == null)
            return false;

        String ssid = wifiService.getConnectionInfo().getSSID();
        if (TextUtils.isEmpty(ssid))
            return false;
        if (ssid.startsWith("\""))
            ssid = ssid.substring(1);
        if (ssid.endsWith("\""))
            ssid = ssid.substring(0, ssid.length() - 1);

        for (String wifi: wifiList) {
            if (wifi.equals(ssid))
                return true;
        }

        return false;
    }

    public static boolean isConnectedToWifi(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifi != null && wifi.isConnected();
    }

    public static boolean isConnectedWifiSecured(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> networkList = wifi.getScanResults();

        //get current connected SSID for comparison to ScanResult
        WifiInfo wi = wifi.getConnectionInfo();
        String currentSSID = wi.getSSID();

        if (networkList != null) {
            for (ScanResult network : networkList)
            {
                //check if current connected SSID
                if (currentSSID.equals(network.SSID)){
                    //get capabilities of current connection
                    String Capabilities =  network.capabilities;

                    if (!Capabilities.toUpperCase().contains("WPA2")
                            && !Capabilities.toUpperCase().contains("PSK")
                            && !Capabilities.toUpperCase().contains("WEP")) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}

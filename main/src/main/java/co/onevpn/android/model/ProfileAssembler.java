package co.onevpn.android.model;

import net.grandcentrix.tray.AppPreferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import co.onevpn.android.OneVpnApp;


public class ProfileAssembler {
    static final String PREF_VPN_CONFIG_SAMPLE = "PREF_VPN_CONFIG_SAMPLE";

    private String getTestOvpn() throws IOException {
        StringBuilder buf = new StringBuilder();
        InputStream json = OneVpnApp.getInstance().getAssets().open("test.ovpn");
        BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str);
        }

        in.close();

        return buf.toString();
    }

    public String assembleConfig(User.Server server, boolean isTest) throws IOException {
        if (isTest)
            return getTestOvpn();

        String connectionMode = OneVpnPreferences.getConnectionMode().toLowerCase();
        String[] test = connectionMode.split(" ");

        AppPreferences preferences = OneVpnApp.getInstance().getPreferences();
        String configSample = preferences.getString(PREF_VPN_CONFIG_SAMPLE, "");
        StringBuilder sb = new StringBuilder(configSample);
        sb.append('\n');
        sb.append("mssfix 1432");
        sb.append('\n');
        sb.append("proto ");
        sb.append(test[0]);
        sb.append('\n');
        sb.append("remote ");
        sb.append(server.getDns());
        sb.append(" ");
        sb.append(test[1]);
        sb.append('\n');

        return sb.toString();
    }

    public String getVpnCA() throws IOException {
        StringBuilder buf = new StringBuilder();
        InputStream json = OneVpnApp.getInstance().getAssets().open("onevpn.ca");
        BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str);
        }

        in.close();

        return buf.toString();
    }

    static void saveConfigSample(String configSample) {
        AppPreferences preferences = OneVpnApp.getInstance().getPreferences();
        preferences.put(ProfileAssembler.PREF_VPN_CONFIG_SAMPLE, configSample);
    }

    static void clearConfigSample() {
        saveConfigSample("");
    }
}

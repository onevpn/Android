package co.onevpn.android.model;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import net.grandcentrix.tray.AppPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.onevpn.android.OneVpnApp;
import co.onevpn.android.log.Logger;
import de.blinkt.openvpn.core.ProfileManager;


public class OneVpnPreferences {
    private static final String PREF_USER_SIGNED_ID = "PREF_USER_SIGNED_ID";
    private static final String PREF_FCM_TOKEN = "PREF_FCM_TOKEN";
    private static final String PREF_USER = "PREF_USER";
    private static final String PREF_CONNECTION_MODE = "PREF_CONNECTION_MODE";
    private static final String PREF_AUTOCONNECT = "PREF_AUTOCONNECT";
    private static final String PREF_BLOCK_COUNTRY = "PREF_BLOCK_COUNTRY";
    private static final String PREF_WHITE_WIFI_LIST = "PREF_WHITE_WIFI_LIST";
    private static final String PREF_LAUNCH_ON_STARTUP = "PREF_LAUNCH_ON_STARTUP";
    private static final String PREF_UNPROTECTED_WIFI = "PREF_UNPROTECTED_WIFI";
    private static final String PREF_FIRST_LAUNCH = "PREF_FIRST_LAUNCH";

    private static AppPreferences preferences = OneVpnApp.getInstance().getPreferences();
    private static Gson gson = new Gson();

    public static void setConnectionMode(String connectionMode, boolean needReassembleProfile) {
        preferences.put(PREF_CONNECTION_MODE, connectionMode);
        Logger.d("set connection mode: " + connectionMode);

        if (needReassembleProfile) {
            User.Server server = UserManager.getSelectedServer();
            new OneVpnProfileManager(UserManager.getInstance().getCurrentUser())
                    .updateOpenVpnConfig(server);
        }

    }

    public static boolean isFirstLaunch() {
        return preferences.getBoolean(PREF_FIRST_LAUNCH, true);
    }

    public static String getConnectionMode() {
        return preferences.getString(PREF_CONNECTION_MODE, "");
    }

    public static List<String> getWhiteWifiList() {
        return gson.fromJson(preferences.getString(PREF_WHITE_WIFI_LIST, "[]"), List.class);
    }

    public static void setWhiteWifiList(@NonNull List<String> wifiList) {
        preferences.put(PREF_WHITE_WIFI_LIST, gson.toJson(wifiList));
    }

    public static void saveFcmToken(String token) {
        preferences.put(PREF_FCM_TOKEN, token);
    }

    public static String getFcmToken() {
        return preferences.getString(PREF_FCM_TOKEN, "");
    }

    public static List<String> getAutoconnectList() {
        return gson.fromJson(preferences.getString(PREF_AUTOCONNECT, "[]"), List.class);
    }

    public static void setAutoconnect(@NonNull List<String> wifiList) {
        preferences.put(PREF_AUTOCONNECT, gson.toJson(wifiList));
    }

    public static Map<String, Boolean> getBlockedCountries() {
        return gson.fromJson(preferences.getString(PREF_BLOCK_COUNTRY, "{}"), Map.class);
    }

    public static void setBlockedCountries(Map<String, Boolean> blockedCountries) {
        preferences.put(PREF_BLOCK_COUNTRY, gson.toJson(blockedCountries));
    }

    public static void setLaunchOnStartup(boolean launchOnStartup) {
        preferences.put(PREF_LAUNCH_ON_STARTUP, launchOnStartup);
        Logger.d("set launchOnStartup = " + launchOnStartup);

        AppPreferences appPreferences = OneVpnApp.getInstance().getPreferences();
        if (launchOnStartup && OneVpnProfileManager.getSelectedProfile() != null) {
            appPreferences.put(ProfileManager.PREF_ALWAYS_VPN,
                    OneVpnProfileManager.getSelectedProfile().getUUIDString());
        } else {
            appPreferences.put(ProfileManager.PREF_ALWAYS_VPN, null);
        }
    }

    public static void setLaunchOnUnprotectedWifi(boolean unprotectedWifi) {
        preferences.put(PREF_UNPROTECTED_WIFI, unprotectedWifi);
    }

    public static boolean getLaunchOnStartup() {
        return preferences.getBoolean(PREF_LAUNCH_ON_STARTUP, false);
    }

    public static boolean isLaunchOnUnprotectedWifi() {
        return preferences.getBoolean(PREF_UNPROTECTED_WIFI, false);
    }

    static boolean isSingedIn() {
        return preferences.getBoolean(PREF_USER_SIGNED_ID, false);
    }

    static User getCurrentUser() {
        return gson.fromJson(preferences.getString(PREF_USER, ""), User.class);
    }

    static void setUserSignIn(boolean signIn, User user) {
        preferences.put(PREF_USER_SIGNED_ID, signIn);

        saveUser(user);

        if (signIn) {
            preferences.put(PREF_FIRST_LAUNCH, false);
            initUserPreferences(user);
        }
        else
            clearUserPreferences();
    }

    static void saveUser(User user) {
        preferences.put(PREF_USER, user != null ? gson.toJson(user) : "");
    }

    private static void clearUserPreferences() {
        preferences.remove(PREF_BLOCK_COUNTRY);
        preferences.remove(PREF_UNPROTECTED_WIFI);
        preferences.remove(PREF_LAUNCH_ON_STARTUP);
        preferences.remove(PREF_AUTOCONNECT);
        preferences.remove(PREF_CONNECTION_MODE);
        preferences.remove(PREF_WHITE_WIFI_LIST);
    }

    private static void initUserPreferences(User user) {
        //init default connection mode
        if (user.getServers().get(0).getProtocol().size() > 0) {
            String connMode = user.getServers().get(0).getProtocol().get(0);
            setConnectionMode(connMode, false);
        }

        //init default blocked countries
        Map<String, Boolean> blockedCountries = new HashMap<>();
        for (User.Server server: user.getServers()) {
            if (!blockedCountries.containsKey(server.getCountry())) {
                blockedCountries.put(server.getCountry(), false);
            }
        }
        setBlockedCountries(blockedCountries);

        List<String> wifiList = new ArrayList<>();
        wifiList.add("Under_a_Roof_free");
        wifiList.add("40Home");
        setAutoconnect(wifiList);
    }
}

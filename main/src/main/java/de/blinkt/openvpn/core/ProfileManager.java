/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import net.grandcentrix.tray.AppPreferences;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import co.onevpn.android.OneVpnApp;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.OneVpnPreferences;
import de.blinkt.openvpn.VpnProfile;

public class ProfileManager {
    private static final String PREFS_NAME = "VPNList";
    private static final String PREFS_VPN_LIST = "vpnlist";
    public static final String PREF_ALWAYS_VPN = "PREF_ALWAYS_VPN";

    private static final String LAST_CONNECTED_PROFILE = "lastConnectedProfile";
    private static ProfileManager instance;

    private static VpnProfile mLastConnectedVpn = null;
    private HashMap<String, VpnProfile> profiles = new HashMap<>();
    private static VpnProfile tmpprofile = null;
    private static Gson gson = new Gson();


    private static VpnProfile get(String key) {
        Logger.d("VpnProfile.tmpprofile == " + tmpprofile);
        if (tmpprofile != null && tmpprofile.getUUIDString().equals(key))
            return tmpprofile;

        if (instance == null) {
            Logger.w("VpnProfile.instance is null!!!");
            return null;
        }

        for (VpnProfile profile : instance.profiles.values()) {
            Logger.w("VpnProfile.list[] = " + profile.getName() + " / " + profile.getUUIDString() );
        }

        return instance.profiles.get(key);

    }


    private ProfileManager() {
    }

    private static void checkInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager();
            instance.loadVPNList(context);
        }
    }

    synchronized public static ProfileManager getInstance(Context context) {
        checkInstance(context);
        return instance;
    }

    public static void setConntectedVpnProfileDisconnected(Context c) {
        final AppPreferences appPreferences = OneVpnApp.getInstance().getPreferences();
        appPreferences.put(LAST_CONNECTED_PROFILE, null);
        Logger.d("setConntectedVpnProfileDisconnected()" );
    }

    /**
     * Sets the profile that is connected (to connect if the service restarts)
     */
    public static void setConnectedVpnProfile(Context c, VpnProfile connectedProfile) {
        Logger.d("setConnectedVpnProfile: " + connectedProfile);
        final AppPreferences appPreferences = OneVpnApp.getInstance().getPreferences();
        appPreferences.put(LAST_CONNECTED_PROFILE, connectedProfile.getUUIDString());
        mLastConnectedVpn = connectedProfile;

    }

    /**
     * Returns the profile that was last connected (to connect if the service restarts)
     */
    public static VpnProfile getLastConnectedProfile(Context c) {
        final AppPreferences appPreferences = OneVpnApp.getInstance().getPreferences();

        String lastConnectedProfile = appPreferences.getString(LAST_CONNECTED_PROFILE, null);
        if (lastConnectedProfile != null)
            return get(c, lastConnectedProfile);
        else
            return null;
    }


    public Collection<VpnProfile> getProfiles() {
        return profiles.values();
    }

    public VpnProfile getProfileByName(String name) {
        for (VpnProfile vpnp : profiles.values()) {
            if (vpnp.getName().equals(name)) {
                return vpnp;
            }
        }
        return null;
    }

    public void saveProfileList(Context context) {
        final AppPreferences appPreferences = OneVpnApp.getInstance().getPreferences();
        appPreferences.put(PREFS_VPN_LIST, gson.toJson(profiles.keySet()));

        // For reasing I do not understand at all
        // Android saves my prefs file only one time
        // if I remove the debug code below :(
        int counter = appPreferences.getInt("counter", 0);
        appPreferences.put("counter", counter + 1);

        Logger.d("ProfileManager.saveProfileList: profile saved" );
    }

    public void addProfile(VpnProfile profile) {
        profiles.put(profile.getUUID().toString(), profile);
    }

    public static void setTemporaryProfile(VpnProfile tmp) {
        ProfileManager.tmpprofile = tmp;
    }

    public static boolean isTempProfile() {
        return mLastConnectedVpn == tmpprofile;
    }


    public void saveProfile(Context context, VpnProfile profile) {
        profile.mVersion += 1;
        ObjectOutputStream vpnfile;
        try {
            vpnfile = new ObjectOutputStream(context.openFileOutput((profile.getUUID().toString() + ".vp"), Activity.MODE_PRIVATE));

            vpnfile.writeObject(profile);
            vpnfile.flush();
            vpnfile.close();
        } catch (IOException e) {
            VpnStatus.logException("saving VPN profile", e);
            throw new RuntimeException(e);
        }
    }


    private void loadVPNList(Context context) {
        profiles = new HashMap<>();
        final AppPreferences appPreferences = OneVpnApp.getInstance().getPreferences();
        Set<String> vlist = gson.fromJson(appPreferences.getString(PREFS_VPN_LIST, null), Set.class);
        if (vlist == null) {
            vlist = new HashSet<>();
        }

        Logger.d("ProfileManager.loadVPNList vlist.size == " + vlist.size());

        for (String vpnentry : vlist) {
            Logger.d("ProfileManager.loadVPNList vpnentry == " + vpnentry);
            try {
                ObjectInputStream vpnfile = new ObjectInputStream(context.openFileInput(vpnentry + ".vp"));
                VpnProfile vp = ((VpnProfile) vpnfile.readObject());

                // Sanity check
                if (vp == null || vp.mName == null || vp.getUUID() == null)
                    continue;

                vp.upgradeProfile();
                profiles.put(vp.getUUID().toString(), vp);

            } catch (IOException | ClassNotFoundException e) {
                VpnStatus.logException("Loading VPN List", e);
            }
        }
    }


    public void removeProfile(Context context, VpnProfile profile) {
        String vpnentry = profile.getUUID().toString();
        profiles.remove(vpnentry);
        saveProfileList(context);
        context.deleteFile(vpnentry + ".vp");
        if (mLastConnectedVpn == profile)
            mLastConnectedVpn = null;

    }

    public static VpnProfile get(Context context, String profileUUID) {
        return get(context, profileUUID, 0, 10);
    }

    public static VpnProfile get(Context context, String profileUUID, int version, int tries) {
        checkInstance(context);
        VpnProfile profile = get(profileUUID);
        Logger.d("VpnProfile get profile with UUID: " + profileUUID);
        Logger.d("VpnProfile get profile returned: " + profile);
        int tried = 0;
        while ((profile == null || profile.mVersion < version) && (tried++ < tries)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            instance.loadVPNList(context);
            profile = get(profileUUID);
            int ver = profile == null ? -1 : profile.mVersion;
        }

        if (tried != 0)

        {
            int ver = profile == null ? -1 : profile.mVersion;
            VpnStatus.logError(String.format("Used x %d tries to get current version (%d/%d) of the profile", tried, ver, version));
        }
        return profile;
    }

    public static VpnProfile getLastConnectedVpn() {
        return mLastConnectedVpn;
    }

    public static VpnProfile getAlwaysOnVPN(Context context) {
        checkInstance(context);
        AppPreferences appPreferences = OneVpnApp.getInstance().getPreferences();
        String uuid = appPreferences.getString(PREF_ALWAYS_VPN, null);
        return get(uuid);

    }
}

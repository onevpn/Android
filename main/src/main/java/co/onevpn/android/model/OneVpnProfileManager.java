package co.onevpn.android.model;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import co.onevpn.android.OneVpnApp;
import co.onevpn.android.R;
import co.onevpn.android.log.Logger;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;


public class OneVpnProfileManager {
    public interface OnUpdateListener {
        void onUpdated();
    }
    private static final String CONFIG_NAME = "onevpn-config.ovpn";

    private ProfileAssembler profileAssembler;
    private VpnProfile mResult;
    private User user;
    private User.Server server;
    private OnUpdateListener onUpdateListener;

    public OneVpnProfileManager(User user) {
        profileAssembler = new ProfileAssembler();
        this.user = user;
    }

    public void updateOpenVpnConfig(User.Server server) {
        updateOpenVpnConfig(server, null);
    }

    public void updateOpenVpnConfig(User.Server server, OnUpdateListener updateListener) {
        this.server = server;
        this.server.setSelected(true);
        this.onUpdateListener = updateListener;
        startImportTask();
    }

    public static VpnProfile getSelectedProfile() {
        ProfileManager profileManager = ProfileManager.getInstance(OneVpnApp.getInstance());
        for (VpnProfile profile: profileManager.getProfiles())
            return profile;

        return null;
    }

    public void clearConfigs() {
        List<VpnProfile> profiles = new ArrayList<>(
                ProfileManager.getInstance(OneVpnApp.getInstance()).getProfiles());
        for (int i = 0; i < profiles.size(); i++) {
            VpnProfile profile = profiles.get(i);
            ProfileManager.getInstance(OneVpnApp.getInstance()).removeProfile(OneVpnApp.getInstance(), profile);
        }
    }

    private void loadCaCert() throws IOException {
        profileAssembler = new ProfileAssembler();
        byte[] fileData = readBytesFromStream(OneVpnApp.getInstance().getAssets().open("onevpn.ca"));
        String prefix = "[[NAME]]onevpn.ca";
        String caData = new String(fileData, "UTF-8");

        String ca = prefix + VpnProfile.INLINE_TAG + caData;
        mResult.mCaFilename = ca;
    }

    private void loadCaCredentialsAndDns() {
        mResult.mUsername = user.getAuth().get("login");
        mResult.mPassword = user.getAuth().get("password");
        mResult.mAuthenticationType = VpnProfile.TYPE_USERPASS;
        mResult.mOverrideDNS = true;
        mResult.mDNS1 = "8.8.8.8";
        mResult.mDNS2 = "8.8.4.4";
    }

    private void startImportTask() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    profileAssembler = new ProfileAssembler();
                    String config = profileAssembler.assembleConfig(server, false);

                    FileOutputStream outputStream;
                    try {
                        outputStream = OneVpnApp.getInstance().openFileOutput(CONFIG_NAME, Context.MODE_PRIVATE);
                        outputStream.write(config.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    boolean isTest = false;
                    InputStream is = !isTest ? OneVpnApp.getInstance().openFileInput(CONFIG_NAME)
                        : OneVpnApp.getInstance().getAssets().open("test.ovpn");

                    doImport(is);
                    if (mResult==null)
                        return -3;

                    loadCaCert();
                    loadCaCredentialsAndDns();

                    trySaveProfile();
                } catch (FileNotFoundException |
                        SecurityException se)

                {
                    Logger.w(R.string.import_content_resolve_error);
                    Logger.w(se.getLocalizedMessage());
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                        checkMarschmallowFileImportError(data);
                    return -2;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return 0;
            }

            @Override
            protected void onPostExecute(Integer errorCode) {
                if (errorCode == 0) {
                    if (onUpdateListener != null) {
                        onUpdateListener.onUpdated();
                    }
                } else {
                    Logger.w(R.string.import_config_error);
                    Toast.makeText(OneVpnApp.getInstance(), R.string.import_config_error, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }.execute();
    }

    private void trySaveProfile() {
        if (mResult == null) {
            Logger.w(R.string.import_config_error);
            Toast.makeText(OneVpnApp.getInstance(), R.string.import_config_error, Toast.LENGTH_LONG).show();
            return;
        }

        mResult.mName = server.getName() + " " + OneVpnPreferences.getConnectionMode();
        saveProfile();
    }


    // use https://github.com/grandcentrix/tray

    private void saveProfile() {
//        Intent result = new Intent();
        ProfileManager vpl = ProfileManager.getInstance(OneVpnApp.getInstance());

        vpl.addProfile(mResult);
        vpl.saveProfile(OneVpnApp.getInstance(), mResult);
        vpl.saveProfileList(OneVpnApp.getInstance());

        Logger.d("OneVpnProfileManager.saveProfile: " + mResult.getUUIDString());
//        result.putExtra(VpnProfile.EXTRA_PROFILEUUID, mResult.getUUID().toString());
    }

    private void doImport(InputStream is) {
        ConfigParser cp = new ConfigParser();
        try {
            InputStreamReader isr = new InputStreamReader(is);

            cp.parseConfig(isr);
            mResult = cp.convertProfile();
            return;
        } catch (IOException | ConfigParser.ConfigParseError e) {
            Logger.w(R.string.error_reading_config_file);
            Logger.w(e.getLocalizedMessage());
        }
        mResult = null;
    }

    static private byte[] readBytesFromStream(InputStream input) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        ;

        long totalread = 0;
        while ((nRead = input.read(data, 0, data.length)) != -1 && totalread <VpnProfile.MAX_EMBED_FILE_SIZE ) {
            buffer.write(data, 0, nRead);
            totalread+=nRead;
        }

        buffer.flush();
        input.close();
        return buffer.toByteArray();
    }
}

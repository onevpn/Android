package co.onevpn.android;

import android.app.Application;
import android.os.Handler;

import net.grandcentrix.tray.AppPreferences;

import co.onevpn.android.log.Logger;
import co.onevpn.android.model.VpnUiStateManager;
import co.onevpn.android.ui.activity.MainActivity;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.PRNGFixes;
import de.blinkt.openvpn.core.StatusListener;
import de.blinkt.openvpn.core.VpnStatus;

public class OneVpnApp extends Application {
    private StatusListener mStatus;

    private static OneVpnApp instance;
    private AppPreferences preferences;
    private Handler handler = new Handler();

    VpnStatus.StateListener stateListener = new VpnStatus.StateListener() {
        @Override
        public void updateState(final String state, final String logmessage, final int localizedResId, final ConnectionStatus level) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    VpnUiStateManager.getInstance().requestUpdateVpnState(OneVpnApp.getInstance());
                }
            });
        }

        @Override
        public void setConnectedVPN(String uuid) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = new AppPreferences(this);

        instance = this;
        PRNGFixes.apply();

        VpnStatus.initLogCache(getApplicationContext().getCacheDir());

        OpenVPNService.setNotificationActivityClass(MainActivity.class);
        mStatus = new StatusListener();
        mStatus.init(getApplicationContext());

        VpnStatus.addStateListener(stateListener);
        Logger.d("OneVpnApp created");
    }

    public static OneVpnApp getInstance() {
        return instance;
    }

    public AppPreferences getPreferences() {
        return preferences;
    }


}

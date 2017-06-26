package co.onevpn.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.model.OneVpnProfileManager;
import co.onevpn.android.model.VpnUiStateManager;
import co.onevpn.android.utils.WifiUtils;
import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.core.VpnStatus;

public class WifiStateReceiver extends BroadcastReceiver {
    private static NetworkInfo lastConnectedNetwork;
    private boolean pendingDisconnect;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable delayDisconnectRunnable = new Runnable() {
        @Override
        public void run() {
            sendDisconnectIntent(OneVpnApp.getInstance());
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (processNetworkChanges(context)) {
            return;
        }

        boolean isActive = VpnStatus.isVPNActive();
        if (!isActive && WifiUtils.isConnectedToWifi(context)) {
            if (!WifiUtils.isConnectedWifiSecured(context)
                    && OneVpnPreferences.isLaunchOnUnprotectedWifi()
                    && !WifiUtils.isWifiContainsInList(context, OneVpnPreferences.getWhiteWifiList())
                || WifiUtils.isWifiContainsInList(context, OneVpnPreferences.getAutoconnectList())) {
                VpnProfile profile = OneVpnProfileManager.getSelectedProfile();
                if(profile != null) {
                    launchVPN(profile, context);
                }
            }
        } else if (isActive
                && WifiUtils.isConnectedToWifi(context)
                && WifiUtils.isWifiContainsInList(context, OneVpnPreferences.getWhiteWifiList())) {
            disconnectVpn();
        }

        VpnUiStateManager.getInstance().requestUpdateVpnState(context);
    }

    private boolean processNetworkChanges(Context context) {
        NetworkInfo networkInfo = WifiUtils.getCurrentNetworkInfo(context);

        if (networkInfo != null) {
            boolean sameNetwork;
            if (lastConnectedNetwork == null
                    || lastConnectedNetwork.getType() != networkInfo.getType()
                    || !equalsObj(lastConnectedNetwork.getExtraInfo(), networkInfo.getExtraInfo())
                    )
                sameNetwork = false;
            else
                sameNetwork = true;

            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
            /* Different network or connection not established anymore */
                if (pendingDisconnect && !sameNetwork) {
                    handler.removeCallbacks(delayDisconnectRunnable);
                    pendingDisconnect = false;
                }

                lastConnectedNetwork = networkInfo;
            }

            return sameNetwork;
        } else {
            if (lastConnectedNetwork != null) {
                lastConnectedNetwork = null;
                return false;
            } else {
                return true;
            }
        }
    }



    private void launchVPN(VpnProfile profile, Context context) {
        Intent startVpnIntent = new Intent(Intent.ACTION_MAIN);
        startVpnIntent.setClass(context, LaunchVPN.class);
        startVpnIntent.putExtra(LaunchVPN.EXTRA_KEY,profile.getUUIDString());
        startVpnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startVpnIntent.putExtra(LaunchVPN.EXTRA_HIDELOG, true);

        context.startActivity(startVpnIntent);
    }

    private void disconnectVpn() {
        pendingDisconnect = true;
        handler.postDelayed(delayDisconnectRunnable, 1000);
    }

    private void sendDisconnectIntent(Context context) {
        Intent disconnectVPN = new Intent(context, DisconnectVPN.class);
        disconnectVPN.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(disconnectVPN);
    }

    public static boolean equalsObj(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}

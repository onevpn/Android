package co.onevpn.android.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Formatter;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import co.onevpn.android.R;
import co.onevpn.android.utils.WifiUtils;
import de.blinkt.openvpn.core.VpnStatus;

public class VpnUiStateManager {
    public enum State {
        NotReachable(R.string.vpn_status_not_reachable, R.drawable.launch_btn_pink, false),
        SecuredCell(R.string.vpn_status_secured_cell, R.drawable.launch_btn_green, true),
        UnsecuredCell(R.string.vpn_status_unsecured_cell, R.drawable.launch_btn_purple, false),
        SecuredWifi(R.string.vpn_status_secured_wifi, R.drawable.launch_btn_blue, true),
        UnsecuredWifi(R.string.vpn_status_unsecured_wifi, R.drawable.launch_btn_yellow, false),
        SecuredTrustedWifi(R.string.vpn_status_secured_trusted_wifi, R.drawable.launch_btn_blue, true),
        UnsecuredTrustedWifi(R.string.vpn_status_unsecured_trusted_wifi, R.drawable.launch_btn_yellow, false);

        public final int statusRes;
        public final int iconRes;
        public final boolean isSecured;

        State(int status, int icon, boolean secured) {
            statusRes = status;
            iconRes = icon;
            isSecured = secured;
        }
    }

    public interface VpnStateChangeListener {
        void onChange(State state, String ipAddress);
    }

    private static VpnUiStateManager instance;
    private VpnUiStateManager() {
    }

    public static synchronized VpnUiStateManager getInstance() {
        if (instance == null) {
            instance = new VpnUiStateManager();
        }

        return instance;
    }

    private State state = State.NotReachable;
    private VpnStateChangeListener stateChangeListener;
    private String ipAddress;

    public void setChangeStateListener(VpnStateChangeListener listener) {
        stateChangeListener = listener;
        if (stateChangeListener != null)
            stateChangeListener.onChange(state, ipAddress);
    }

    private void notifyStateChanged(State state) {
        this.state = state;
        if (stateChangeListener != null) {
            stateChangeListener.onChange(state, ipAddress);
        }
    }

    public void updateIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        if (stateChangeListener != null) {
            stateChangeListener.onChange(state, ipAddress);
        }
    }

    public void requestUpdateVpnState(Context context) {
        boolean isVpnActive = VpnStatus.isVPNActive();
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        String ip = getLocalIpAddress();

        if (wifi.isConnectedOrConnecting()) {
            if (WifiUtils.isWifiContainsInList(context, OneVpnPreferences.getWhiteWifiList())) {
                notifyStateChanged(isVpnActive ? VpnUiStateManager.State.SecuredTrustedWifi
                        : VpnUiStateManager.State.UnsecuredTrustedWifi);
            } else {
                notifyStateChanged(isVpnActive ? VpnUiStateManager.State.SecuredWifi
                        : VpnUiStateManager.State.UnsecuredWifi);
            }

            if (!isVpnActive)
                updateIpAddress(ip);
        } else if (mobile.isConnectedOrConnecting()) {
            notifyStateChanged(isVpnActive ? VpnUiStateManager.State.SecuredCell
                    : VpnUiStateManager.State.UnsecuredCell);

            if (!isVpnActive)
                updateIpAddress(ip);
        } else {
            // No network
            notifyStateChanged(VpnUiStateManager.State.NotReachable);
            updateIpAddress(null);
        }
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        Log.i("vpn-ui", "***** IP="+ ip);
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("vpn-ui", ex.toString());
        }
        return null;
    }

}

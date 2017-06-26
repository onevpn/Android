package co.onevpn.android.ui.contract;

import de.blinkt.openvpn.core.ConnectionStatus;


public interface LaunchVpnContract {
    void updateVpnState(String state, final String logMessage, int localizedResId, ConnectionStatus level);
    void showWifiSuggestion(String wifi);
    void showProgress(boolean show);
}

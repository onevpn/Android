package co.onevpn.android.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import co.onevpn.android.Constants;
import co.onevpn.android.OneVpnApp;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.model.OneVpnProfileManager;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.service.UserBandwidthService;
import co.onevpn.android.ui.activity.BaseActivity;
import co.onevpn.android.ui.activity.EnergySettingsActivity;
import co.onevpn.android.ui.activity.MainActivity;
import co.onevpn.android.ui.activity.ServersActivity;
import co.onevpn.android.ui.contract.LaunchVpnContract;
import co.onevpn.android.utils.WifiUtils;
import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;
import easymvp.AbstractPresenter;


public class LaunchVpnPresenter  extends AbstractPresenter<LaunchVpnContract> {
    private boolean isInit;
    private Handler handler = new Handler();

    VpnStatus.StateListener stateListener = new VpnStatus.StateListener() {
        @Override
        public void updateState(final String state, final String logmessage, final int localizedResId, final ConnectionStatus level) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (getView() != null) {
                        getView().updateVpnState(state, logmessage, localizedResId, level);
                    }
                }
            });
        }

        @Override
        public void setConnectedVPN(String uuid) {
        }
    };

    public void initVpnListeners() {
        if (isInit)
            return;
        VpnStatus.addStateListener(stateListener);

        isInit = true;

        Logger.d("VPN listeners inited");
    }

    public void clearVpnListeners() {
        if (!isInit)
            return;

        VpnStatus.removeStateListener(stateListener);
        isInit = false;

        Logger.d("VPN listeners removed");
    }

    public void openServers(Activity activity) {
        activity.startActivity(new Intent(activity, ServersActivity.class));
    }

    public void toggleVPN(final BaseActivity context) {
        VpnProfile profile = OneVpnProfileManager.getSelectedProfile();

        /*
         * User could terminate app while new profile is configuring
         */
        if (profile == null) {
            OneVpnProfileManager profileManager = new OneVpnProfileManager(UserManager.getInstance().getCurrentUser());
            profileManager.clearConfigs();
            getView().showProgress(true);
            profileManager.updateOpenVpnConfig(UserManager.getSelectedServer(), new OneVpnProfileManager.OnUpdateListener() {
                @Override
                public void onUpdated() {
                    VpnProfile profile = OneVpnProfileManager.getSelectedProfile();
                    if (getView() != null) {
                        getView().showProgress(false);
                    }
                    processToggleVpn(context, profile);
                }
            });
        } else {
            processToggleVpn(context, profile);
        }
    }

    private void processToggleVpn(BaseActivity context, VpnProfile profile) {
        if (VpnStatus.isVPNActive() && profile.getUUIDString().equals(VpnStatus.getLastConnectedVPNProfile())) {
            suggestAddWIfiInExceptionIfNeeded(context);
            Intent disconnectVPN = new Intent(context, DisconnectVPN.class);
            context.startActivity(disconnectVPN);
            context.trackAction(Constants.ANALYTICS_ACTION_VPN_OFF);
        } else {
            startVPN(context, profile);

            //run service for updating traffic limits
            Intent intent = new Intent(context, UserBandwidthService.class);
            context.startService(intent);
            context.trackAction(Constants.ANALYTICS_ACTION_VPN_ON);
        }
    }

    private void suggestAddWIfiInExceptionIfNeeded(Context context) {
        if (WifiUtils.isConnectedToWifi(context)
                && !WifiUtils.isConnectedWifiSecured(context)
                && OneVpnPreferences.isLaunchOnUnprotectedWifi()
                && !WifiUtils.isWifiContainsInList(context, OneVpnPreferences.getWhiteWifiList())) {
            String ssid = WifiUtils.getCurrentNetworkInfo(context).getExtraInfo();
            if (ssid.startsWith("\""))
                ssid = ssid.substring(1);
            if (ssid.endsWith("\""))
                ssid = ssid.substring(0, ssid.length() - 1);
            if (getView() != null) {
                getView().showWifiSuggestion(ssid);
            }
        }
    }

    private void startVPN(Context context, VpnProfile profile) {
        if (showEnergySettingsScreen(context)) {
            return;
        }

        getPM(context).saveProfile(context, profile);

        Intent intent = new Intent(context, LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
        intent.putExtra(LaunchVPN.EXTRA_HIDELOG, true);
        intent.setAction(Intent.ACTION_MAIN);
        context.startActivity(intent);
    }

    private ProfileManager getPM(Context context) {
        return ProfileManager.getInstance(context);
    }

    private boolean showEnergySettingsScreen(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                OneVpnApp.getInstance().getPreferences().getBoolean(MainActivity.PREF_SHOW_ENERGY_SETTINGS, true)) {
            context.startActivity(new Intent(context, EnergySettingsActivity.class));
            return true;
        }

        return false;
    }
}

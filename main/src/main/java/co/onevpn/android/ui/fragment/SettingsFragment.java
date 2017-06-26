package co.onevpn.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import co.onevpn.android.Constants;
import co.onevpn.android.R;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.ui.dialog.BlockCountryDialog;
import co.onevpn.android.ui.dialog.ConnectionModeDialog;
import co.onevpn.android.ui.dialog.EditableListDialog;

public class SettingsFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private SwitchCompat launchOnStartup;
    private SwitchCompat unprotectedWifi;

    public SettingsFragment() {
        super();

        screenName = "Settings";
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        trackHit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        launchOnStartup = fbid(view, R.id.setting_launch_on_startup);
        launchOnStartup.setOnCheckedChangeListener(this);
        fbid(view, R.id.setting_autoconnect_label).setOnClickListener(this);
        fbid(view, R.id.setting_block_country_btn).setOnClickListener(this);
        fbid(view, R.id.setting_connection_mode_label).setOnClickListener(this);
        fbid(view, R.id.setting_white_wifi_list_label).setOnClickListener(this);
        unprotectedWifi = fbid(view, R.id.setting_unprotected_wifi);
        unprotectedWifi.setOnCheckedChangeListener(this);

        unprotectedWifi.setChecked(OneVpnPreferences.isLaunchOnUnprotectedWifi());
        launchOnStartup.setChecked(OneVpnPreferences.getLaunchOnStartup());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting_autoconnect_label:
                EditableListDialog autoconnectDialog = EditableListDialog.newInstance(
                        OneVpnPreferences.getAutoconnectList(), "Autoconnect to:");
                autoconnectDialog.show(getActivity().getSupportFragmentManager(), "AutoconnectDialog");
                trackAction(Constants.ANALYTICS_ACTION_SETTING_AUTOCONNECT);
                break;
            case R.id.setting_block_country_btn:
                BlockCountryDialog countryDialog = new BlockCountryDialog();
                countryDialog.show(getActivity().getSupportFragmentManager(), "BlockCountryDialog");
                trackAction(Constants.ANALYTICS_ACTION_SETTING_BLOCK_COUNTRY);
                break;
            case R.id.setting_connection_mode_label:
                DialogFragment connDialog = new ConnectionModeDialog();
                connDialog.show(getActivity().getSupportFragmentManager(), "ConnectionModeDialog");
                trackAction(Constants.ANALYTICS_ACTION_SETTING_CONNECTION_MODE);
                break;
            case R.id.setting_white_wifi_list_label:
                EditableListDialog whiteWifiDialog = EditableListDialog.newInstance(
                        OneVpnPreferences.getWhiteWifiList(), "White WiFi list");
                whiteWifiDialog.show(getActivity().getSupportFragmentManager(), "WhiteWifiListDialog");
                trackAction(Constants.ANALYTICS_ACTION_SETTING_WIFI_WHITELIST);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.setting_launch_on_startup:
                OneVpnPreferences.setLaunchOnStartup(b);
                launchOnStartup.setChecked(b);
                trackAction(Constants.ANALYTICS_ACTION_SETTING_LAUNCH_ON_STARTUP);
                break;
            case R.id.setting_unprotected_wifi:
                OneVpnPreferences.setLaunchOnUnprotectedWifi(b);
                unprotectedWifi.setChecked(b);
                trackAction(Constants.ANALYTICS_ACTION_SETTING_UNPROTECTED_WIFI);
                break;
        }
    }
}

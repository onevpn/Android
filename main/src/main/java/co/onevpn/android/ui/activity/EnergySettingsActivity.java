package co.onevpn.android.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import net.grandcentrix.tray.AppPreferences;

import co.onevpn.android.OneVpnApp;
import co.onevpn.android.R;

/**
 * Created by sergeygorun on 6/18/17.
 */

public class EnergySettingsActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_energy_settings);

        findViewById(R.id.keep_enabled).setOnClickListener(this);
        findViewById(R.id.disable).setOnClickListener(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestDozeDisable() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(packageName))
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.keep_enabled) {
            AppPreferences preferences = OneVpnApp.getInstance().getPreferences();
            preferences.put(MainActivity.PREF_SHOW_ENERGY_SETTINGS, false);

        } else if (v.getId() == R.id.disable) {
            requestDozeDisable();
            AppPreferences preferences = OneVpnApp.getInstance().getPreferences();
            preferences.put(MainActivity.PREF_SHOW_ENERGY_SETTINGS, false);
            finish();
        }
    }
}

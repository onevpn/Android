package co.onevpn.android.ui.presenter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import co.onevpn.android.Constants;
import co.onevpn.android.OneVpnApp;
import co.onevpn.android.api.LoginRequest;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.ui.activity.EnergySettingsActivity;
import co.onevpn.android.ui.activity.LoginActivity;
import co.onevpn.android.ui.activity.MainActivity;
import co.onevpn.android.ui.activity.SettingsActivity;
import co.onevpn.android.ui.activity.TrialExpiredActivity;
import co.onevpn.android.ui.contract.MainContract;
import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.core.VpnStatus;
import easymvp.AbstractPresenter;



public class MainPresenter extends AbstractPresenter<MainContract> {
    private BroadcastReceiver paymentBroadcastReceiver;

    public void upgradeToPremium(Activity activity) {
        String url = UserManager.getInstance().getCurrentUser().getPay().get("url");
        Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(signUpIntent);
    }

    public void changePassword(Activity activity) {
        Toast.makeText(activity, "Change password service stub", Toast.LENGTH_SHORT).show();
    }

    public void rateOutService(Activity activity) {
        Toast.makeText(activity, "Rate our service stub", Toast.LENGTH_SHORT).show();
    }

    public void openSettings(Activity activity) {
        activity.startActivity(new Intent(activity, SettingsActivity.class));
    }

    public void showTrialExpired(Activity activity) {
        activity.startActivity(new Intent(activity, TrialExpiredActivity.class));
        activity.finish();
    }


    public void signOut(Activity activity) {
        LoginRequest loginRequest = new LoginRequest(UserManager.getInstance().getCurrentUser().getEmail(),
                UserManager.getInstance().getCurrentUser().getPassword(), "");
        loginRequest.execute(null);

        UserManager.getInstance().signOut();

        if (VpnStatus.isVPNActive()) {
            Intent disconnectVPN = new Intent(activity, DisconnectVPN.class);
            disconnectVPN.putExtra(LoginActivity.EXTRA_OPEN_LOGIN, true);
            activity.startActivity(disconnectVPN);
            activity.finish();
        } else {
            activity.startActivity(new Intent(activity, LoginActivity.class));
            activity.finish();
        }
    }



    public void registerPaymentReceiver(Context context) {
        paymentBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (getView() != null
                    && UserManager.getInstance().getCurrentUser() != null)
                    getView().updateNavHeader();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_PAYMENT_SUCCESS);
        context.registerReceiver(paymentBroadcastReceiver, intentFilter);
    }

    public void unregisterPaymentReceiver(Context context) {
        if (paymentBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(paymentBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

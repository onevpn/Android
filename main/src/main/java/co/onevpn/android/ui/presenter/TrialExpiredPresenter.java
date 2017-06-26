package co.onevpn.android.ui.presenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import co.onevpn.android.Constants;
import co.onevpn.android.api.LoginRequest;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.ui.activity.LoginActivity;
import co.onevpn.android.ui.activity.MainActivity;
import co.onevpn.android.ui.contract.TrialExpiredContract;
import easymvp.AbstractPresenter;

public class TrialExpiredPresenter extends AbstractPresenter<TrialExpiredContract> {
    private BroadcastReceiver paymentBroadcastReceiver;

    public void upgradeToPremium(Activity activity) {
        String url = UserManager.getInstance().getCurrentUser().getPay().get("url");
        Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(signUpIntent);
    }

    public void signOut(Activity activity) {
        LoginRequest loginRequest = new LoginRequest(UserManager.getInstance().getCurrentUser().getEmail(),
                UserManager.getInstance().getCurrentUser().getPassword(), "");
        loginRequest.execute(null);

        UserManager.getInstance().signOut();

        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();
    }

    public void showMainActivity(Activity activity) {
        activity.startActivity(new Intent(activity, MainActivity.class));
        activity.finish();
    }

    public void checkPayPlan() {
        if (getView() != null
                && UserManager.getInstance().getCurrentUser() != null
                && UserManager.getInstance().getCurrentUser().getPlan().getPlanExpired() > 0)
            getView().payPlanUpgraded();
    }

    public void registerPaymentReceiver(Context context) {
        paymentBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                checkPayPlan();
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

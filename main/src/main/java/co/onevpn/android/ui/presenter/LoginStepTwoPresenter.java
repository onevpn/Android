package co.onevpn.android.ui.presenter;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import co.onevpn.android.Constants;
import co.onevpn.android.OneVpnApp;
import co.onevpn.android.R;
import co.onevpn.android.api.BaseApiRequest;
import co.onevpn.android.api.LoginRequest;
import co.onevpn.android.model.ConfigLoader;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.model.OneVpnProfileManager;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.ui.contract.LoginTwoStepContract;
import easymvp.AbstractPresenter;


public class LoginStepTwoPresenter extends AbstractPresenter<LoginTwoStepContract> {
    private Handler handler = new Handler();

    public void signIn(final String login, final String password) {
        if (OneVpnPreferences.isFirstLaunch()) {
            getView().showFirstLaunchProgress(true);
        } else {
            getView().showProgress(true);
        }
        String token = OneVpnPreferences.getFcmToken();
        final String hashedPassword = UserManager.getHashedPassword(password);

        LoginRequest loginRequest = new LoginRequest(login, hashedPassword, token);
        loginRequest.execute(new BaseApiRequest.Callback<User>() {
            @Override
            public void onSuccess(final User user) {
                ConfigLoader configLoader = new ConfigLoader(new ConfigLoader.OnConfigSampleLoad() {
                    @Override
                    public void onLoad() {
                        handler.removeCallbacksAndMessages(null);
                        user.setEmail(login);
                        user.setPassword(hashedPassword);
                        User.Server selectedServer = user.getServers().get(0);
                        selectedServer.setSelected(true);
                        UserManager.getInstance().signIn(user);
                        new OneVpnProfileManager(user).updateOpenVpnConfig(selectedServer);
                        if (getView() != null) {
                            getView().processSuccess();
                            getView().showProgress(false);
                        }
                    }

                    @Override
                    public void onFailed() {
                        handler.removeCallbacksAndMessages(null);
                        if (getView() != null) {
                            getView().showError(OneVpnApp.getInstance().getString(R.string.error_internet_connection));
                            getView().showProgress(false);
                            getView().showFirstLaunchProgress(false);
                        }
                    }
                });

                configLoader.fetchVpnConfigSample(login, password);
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getView() != null) {
                    if (errorMessage != null) {
                        getView().showError(errorMessage);
                    }
                    getView().showProgress(false);
                    getView().showFirstLaunchProgress(false);
                }
                handler.removeCallbacksAndMessages(null);

            }
        });
    }

    public void signUp(Activity activity) {
        try {
            Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_SIGN_UP));
            activity.startActivity(signUpIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "cannot open url in browser", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void forgot(Activity activity) {
        try {
            Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_FORGOT));
            activity.startActivity(signUpIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "cannot open url in browser", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setupFirstLaunchStatuses() {
        int[] statuses = {
                R.string.first_launch_progress_1,
                R.string.first_launch_progress_2,
                R.string.first_launch_progress_3,
                R.string.first_launch_progress_4,
                R.string.first_launch_progress_5,
                R.string.first_launch_progress_6,
                R.string.first_launch_progress_7
        };

        Context context = OneVpnApp.getInstance();

        for (int i = 0; i < statuses.length; i++) {
            int delay = 2500 * i + 100;
            new LoginStepTwoPresenter.ShowProgressText(context, statuses[i], delay).showProgress();
        }
    }

    class ShowProgressText implements Runnable {
        private String text;
        private int delay;

        ShowProgressText(Context context, int text, int delay) {
            this.text = context.getResources().getString(text);
            this.delay = delay;
        }

        @Override
        public void run() {
            if (getView() != null) {
                getView().setFirstLaunchProgressText(text);
            }
        }

        void showProgress() {
            handler.postDelayed(this, delay);
        }
    }
}

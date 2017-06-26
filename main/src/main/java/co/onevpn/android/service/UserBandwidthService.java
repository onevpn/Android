package co.onevpn.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import co.onevpn.android.api.BaseApiRequest;
import co.onevpn.android.api.LoginRequest;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.VpnStatus;

public class UserBandwidthService extends Service {
    private Handler handler = new Handler();
    private boolean isRunning;

    VpnStatus.StateListener stateListener = new VpnStatus.StateListener() {
        @Override
        public void updateState(final String state, final String logmessage, final int localizedResId, final ConnectionStatus level) {
            if (isRunning && !VpnStatus.isVPNActive()) {
                handler.removeCallbacks(bandwidthRunnable);
                stopSelf();
            }

            if (!isRunning && VpnStatus.isVPNActive()) {
                isRunning = true;
            }
        }

        @Override
        public void setConnectedVPN(String uuid) {

        }
    };

    private Runnable bandwidthRunnable = new Runnable() {
        @Override
        public void run() {
            final User user = UserManager.getInstance().getCurrentUser();
            if (user != null) {
                LoginRequest loginRequest = new LoginRequest(user.getEmail(),
                        user.getPassword(), OneVpnPreferences.getFcmToken());
                loginRequest.execute(new BaseApiRequest.Callback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        user.setBandwidth(result.getBandwidth());
                        if (UserManager.getInstance().isSingedIn())
                            UserManager.getInstance().update(user);

                        Logger.d("bandwidth: " + result.getBandwidth().get("limit"));
                    }

                    @Override
                    public void onFailure(String errorMessage) {

                    }
                });

                Logger.d("send bandwidth request");

                requestBandwidth();
            }
        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VpnStatus.addStateListener(stateListener);
        Logger.d("bandwidth service created");

        requestBandwidth();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("bandwidth service destroyed");
        handler.removeCallbacks(bandwidthRunnable);
    }

    private void requestBandwidth() {
        handler.postDelayed(bandwidthRunnable, 30 * 1000);
    }
}

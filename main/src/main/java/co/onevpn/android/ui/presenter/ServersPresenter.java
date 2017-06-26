package co.onevpn.android.ui.presenter;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;

import co.onevpn.android.model.OneVpnProfileManager;
import co.onevpn.android.model.PingManager;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.ui.contract.ServersContract;
import easymvp.AbstractPresenter;



public class ServersPresenter extends AbstractPresenter<ServersContract> {
    private Handler handler = new Handler();
    private static final int TIMEOUT = 3000; //we need time to update vpn profiles

    public void runPingService(@NonNull Activity activity, PingManager.OnProgressResult progressResult, boolean force) {
        PingManager.getInstance().setProgressResult(progressResult);
        PingManager.getInstance().runService(activity);
    }

    public void toggleServerFavorite(User.Server server) {
        server.setFavorite(!server.isFavorite());
        UserManager.getInstance().update(UserManager.getInstance().getCurrentUser());
        getView().refreshServerList();
    }

    public void toggleServerSelected(@NonNull final Activity activity, User.Server server) {
        getView().showProgress(true);
        for (User.Server s: UserManager.getInstance().getCurrentUser().getServers()) {
            if (s.equals(server) && s.isSelected()) {
                getView().showProgress(false);
                return;
            }
            s.setSelected(false);
        }

        server.setSelected(true);
        UserManager.getInstance().update(UserManager.getInstance().getCurrentUser());
        OneVpnProfileManager profileManager = new OneVpnProfileManager(UserManager.getInstance().getCurrentUser());
        profileManager.clearConfigs();
        profileManager.updateOpenVpnConfig(server, new OneVpnProfileManager.OnUpdateListener() {
            @Override
            public void onUpdated() {
                if (getView() != null) {
                    getView().showProgress(false);
                    activity.finish();
                }
            }
        });
    }

}

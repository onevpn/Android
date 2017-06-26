package co.onevpn.android.service;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import co.onevpn.android.api.LoginRequest;
import co.onevpn.android.api.OneVpnService;
import co.onevpn.android.api.RestFabric;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;

public class OneVpnFirebaseInstanceIdService extends FirebaseInstanceIdService {
    OneVpnService gitHubService = RestFabric.get().create(OneVpnService.class);

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Logger.d("Refreshed token: " + refreshedToken);
        OneVpnPreferences.saveFcmToken(refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        User user = UserManager.getInstance().getCurrentUser();
        if (user != null) {
            LoginRequest loginRequest = new LoginRequest(user.getEmail(), user.getPassword(), token);
            loginRequest.execute(null);
        }
    }
}

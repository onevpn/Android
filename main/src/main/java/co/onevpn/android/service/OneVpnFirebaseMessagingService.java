package co.onevpn.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import co.onevpn.android.Constants;
import co.onevpn.android.OneVpnApp;
import co.onevpn.android.R;
import co.onevpn.android.api.BaseApiRequest;
import co.onevpn.android.api.LoginRequest;
import co.onevpn.android.api.response.PaymentPush;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.ui.activity.MainActivity;


public class OneVpnFirebaseMessagingService extends FirebaseMessagingService {
    private Gson gson = new Gson();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Logger.d("From: " + remoteMessage.getFrom());



        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0 && remoteMessage.getData().containsKey("type")) {
            Logger.d("Message data payload: " + remoteMessage.getData());
            int type = Integer.parseInt(remoteMessage.getData().get("type"));
            String payload = remoteMessage.getData().get("pyload");

            if (UserManager.getInstance().getCurrentUser() == null)
                return;

            switch (type) {
                case 1:
                    processPayPush(payload);
                    break;
                default:
                    Logger.w("Received unknown type: " + type);
            }
        }
    }

    private void processPayPush(String rawPayload) {
        if (rawPayload != null) {
            final PaymentPush payPush = gson.fromJson(rawPayload, PaymentPush.class);

            //skip
            if (!UserManager.getInstance().getCurrentUser().getEmail().equals(payPush.getEmail()))
                return;

            LoginRequest loginRequest = new LoginRequest(
                    UserManager.getInstance().getCurrentUser().getEmail(),
                    UserManager.getInstance().getCurrentUser().getPassword(),
                    OneVpnPreferences.getFcmToken());

            loginRequest.execute(new BaseApiRequest.Callback<User>() {

                @Override
                public void onSuccess(User user) {
                    user.setEmail(UserManager.getInstance().getCurrentUser().getEmail());
                    user.setPassword(UserManager.getInstance().getCurrentUser().getPassword());
                    user.setServers(UserManager.getInstance().getCurrentUser().getServers());
                    UserManager.getInstance().update(user);
                    sendNotification(payPush.getMessage());
                    sendBroadcastAction();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(OneVpnApp.getInstance(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("OneVPN")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendBroadcastAction() {
        UserManager.getInstance().getCurrentUser().getPlan().setPlanExpired(100);
        Intent in = new Intent(Constants.ACTION_PAYMENT_SUCCESS);
        sendBroadcast(in);
    }
}

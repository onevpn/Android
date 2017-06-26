package co.onevpn.android.model;

import android.app.Activity;
import android.content.Intent;
import android.util.Pair;

import co.onevpn.android.service.PingService;

public class PingManager {

    public interface OnProgressResult {
        void onProgress(Pair<User.Server, Integer> progress);
    }

    private static PingManager ourInstance;

    private OnProgressResult progressResult;

    private PingManager() {
    }

    public static PingManager getInstance() {
        if (ourInstance == null)
            ourInstance = new PingManager();
        return ourInstance;
    }

    public void runService(Activity activity) {
        Intent pingIntent = new Intent(activity, PingService.class);
        pingIntent.setAction(PingService.ACTION_START_PING_TASK);
        activity.startService(pingIntent);
    }

    public OnProgressResult getProgressResult() {
        return progressResult;
    }

    public void setProgressResult(OnProgressResult progressResult) {
        this.progressResult = progressResult;
    }


}

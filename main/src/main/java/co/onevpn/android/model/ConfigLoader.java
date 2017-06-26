package co.onevpn.android.model;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.io.IOException;

import co.onevpn.android.api.BaseApiRequest;
import co.onevpn.android.api.ConfigRequest;
import co.onevpn.android.api.OneVpnService;
import co.onevpn.android.api.RestFabric;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ConfigLoader {
    public interface OnConfigSampleLoad {
        void onLoad();
        void onFailed();
    }

    private static final int TIMEOUT = 5000;
    private static final int TOTAL_TRIES = 3;

    private int tries = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private OnConfigSampleLoad configSampleLoad;
    public ConfigLoader(@NonNull OnConfigSampleLoad configSampleLoad) {
        this.configSampleLoad = configSampleLoad;
    }

    public void fetchVpnConfigSample(final String login, final String password) {
        tries++;

        ConfigRequest configRequest = new ConfigRequest(login,
                UserManager.getHashedPassword(password));
        configRequest.execute(new BaseApiRequest.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                ProfileAssembler.saveConfigSample(result);
                configSampleLoad.onLoad();
            }

            @Override
            public void onFailure(String errorMessage) {
                retryOrFail(login, password);
            }
        });
    }

    private void retryOrFail(final String login, final String password) {
        if (tries < TOTAL_TRIES)
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchVpnConfigSample(login, password);
                }
            }, TIMEOUT);
        else
            configSampleLoad.onFailed();
    }

}

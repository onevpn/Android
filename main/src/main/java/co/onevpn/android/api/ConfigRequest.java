package co.onevpn.android.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import co.onevpn.android.model.ProfileAssembler;
import co.onevpn.android.model.UserManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigRequest extends BaseApiRequest<String> {
    private String login;
    private String password;

    public ConfigRequest(@NonNull String login, @NonNull String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public void execute(final Callback<String> callback) {
        if (callback == null)
            throw new NullPointerException("Callback cannot be null");

        Call<ResponseBody> call = gitHubService.getVpnConfigSample("getconfig", login, password);

        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                if (body == null)
                    callback.onFailure(null);
                else {
                    try {
                        callback.onSuccess(body.string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.onFailure(null);
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure(null);
            }
        });
    }
}

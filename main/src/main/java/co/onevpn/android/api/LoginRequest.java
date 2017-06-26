package co.onevpn.android.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.onevpn.android.OneVpnApp;
import co.onevpn.android.R;
import co.onevpn.android.model.ConfigLoader;
import co.onevpn.android.model.OneVpnProfileManager;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRequest extends BaseApiRequest<User> {
    private String login;
    private String password;
    private String token;

    public LoginRequest(@NonNull String login, @NonNull String password,
                        @Nullable String token) {
        this.login = login;
        this.password = password;
        this.token = token;
    }

    @Override
    public void execute(final Callback<User> callback) {
        Call<User> call = gitHubService.listRepos("auth", login, password, token);

        call.enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                final User user = response.body();
                if (user == null || user.getMessage() == null || !user.getMessage().containsKey("value")) {
                    if (callback != null)
                        callback.onFailure(OneVpnApp.getInstance().getString(R.string.error_internet_connection));
                } else if (user.getMessage().get("value").startsWith("Error")) {
                    if (callback != null)
                        callback.onFailure(user.getMessage().get("value"));
                } else {
                    if (callback != null) {
                        callback.onSuccess(user);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (callback != null)
                    callback.onFailure(OneVpnApp.getInstance().getString(R.string.error_internet_connection));
            }
        });
    }
}

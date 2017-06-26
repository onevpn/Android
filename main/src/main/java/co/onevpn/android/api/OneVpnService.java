package co.onevpn.android.api;

import co.onevpn.android.model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OneVpnService {
    @FormUrlEncoded @POST("api/")
    Call<User> listRepos(
        @Field("mode") String mode,
        @Field("login") String login,
        @Field("pass") String password,
        @Field("token") String token
    );

    @FormUrlEncoded @POST("api/")
    Call<ResponseBody> getVpnConfigSample(
        @Field("mode") String mode,
        @Field("login") String login,
        @Field("pass") String password
    );
}

package co.onevpn.android.api;

import java.util.concurrent.TimeUnit;

import co.onevpn.android.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestFabric {

    private static final Retrofit retrofit;

    static {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.APP_DOMAIN + "/members/mobile/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit get() {
        return retrofit;
    }
}

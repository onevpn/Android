package co.onevpn.android.api;


public abstract class BaseApiRequest<T>  {
    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

    OneVpnService gitHubService = RestFabric.get().create(OneVpnService.class);
    public abstract void execute(Callback<T> callback);
}

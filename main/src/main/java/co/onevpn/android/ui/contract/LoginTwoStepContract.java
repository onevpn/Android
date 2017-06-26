package co.onevpn.android.ui.contract;


public interface LoginTwoStepContract {
    void showProgress(boolean show);
    void showFirstLaunchProgress(boolean show);
    void setFirstLaunchProgressText(String text);
    void showError(String error);
    void processSuccess();
}

package co.onevpn.android.ui.presenter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import co.onevpn.android.Constants;
import co.onevpn.android.ui.contract.LoginOneStepContract;
import easymvp.AbstractPresenter;


public class LoginStepOnePresenter extends AbstractPresenter<LoginOneStepContract> {
    public void clickHaveBtn() {
        getView().showLoginPanel();
    }

    public void clickHaventBtn(Activity activity) {
        try {
            Intent signUpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_SIGN_UP));
            activity.startActivity(signUpIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "cannot open url in browser", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}

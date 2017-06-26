package co.onevpn.android.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import co.onevpn.android.R;
import co.onevpn.android.ui.fragment.LoginStepOne;
import co.onevpn.android.ui.fragment.LoginStepTwo;


public class LoginActivity extends BaseActivity implements LoginStepOne.LoginStepInteractor {
    public static final String EXTRA_OPEN_LOGIN = "EXTRA_OPEN_LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_new);
        initToolbar();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.root, LoginStepOne.newInstance())
                .commit();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.login_enter_in_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void moveToStepTwo() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root, LoginStepTwo.newInstance())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack("login-step-two")
                .commit();
    }


}

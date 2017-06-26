package co.onevpn.android.ui.activity;

import android.os.Bundle;
import android.view.View;

import co.onevpn.android.Constants;
import co.onevpn.android.R;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.ui.contract.TrialExpiredContract;
import co.onevpn.android.ui.presenter.TrialExpiredPresenter;
import easymvp.annotation.ActivityView;
import easymvp.annotation.Presenter;

@ActivityView(layout = R.layout.activity_trial, presenter = TrialExpiredPresenter.class)
public class TrialExpiredActivity extends BaseActivity implements TrialExpiredContract {
    @Presenter
    TrialExpiredPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fbid(R.id.get_premium_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.upgradeToPremium(TrialExpiredActivity.this);
                trackAction(Constants.ANALYTICS_ACTION_UPGRADE_TO_PREMIUM);
            }
        });

        fbid(R.id.change_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.signOut(TrialExpiredActivity.this);
                trackAction(Constants.ANALYTICS_ACTION_CHANGE_ACCOUNT);
            }
        });

        screenName = "TrialExpired";
        trackHit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        presenter.registerPaymentReceiver(this);
        presenter.checkPayPlan();
    }

    @Override
    protected void onStop() {
        super.onStop();

        presenter.unregisterPaymentReceiver(this);
    }

    @Override
    public void payPlanUpgraded() {
        Logger.d("pay plan upgraded!");
        presenter.showMainActivity(this);
    }

}

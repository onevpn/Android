package co.onevpn.android.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import co.onevpn.android.R;
import co.onevpn.android.ui.contract.SettingsContract;
import co.onevpn.android.ui.fragment.SettingsFragment;
import co.onevpn.android.ui.presenter.SettingsPresenter;
import easymvp.annotation.ActivityView;
import easymvp.annotation.Presenter;

@ActivityView(layout = R.layout.activity_settings, presenter = SettingsPresenter.class)
public class SettingsActivity extends BaseActivity implements SettingsContract {
    @Presenter
    SettingsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_settings, SettingsFragment.newInstance())
                .commit();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

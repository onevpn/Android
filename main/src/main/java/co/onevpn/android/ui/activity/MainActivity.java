package co.onevpn.android.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.onevpn.android.Constants;
import co.onevpn.android.R;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.ui.contract.MainContract;
import co.onevpn.android.ui.fragment.LaunchVpnFragment;
import co.onevpn.android.ui.presenter.MainPresenter;
import easymvp.annotation.ActivityView;
import easymvp.annotation.Presenter;


@ActivityView(layout = R.layout.activity_main, presenter = MainPresenter.class)
public class MainActivity extends BaseActivity
        implements MainContract, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    public static final String PREF_SHOW_ENERGY_SETTINGS = "PREF_SHOW_ENERGY_SETTINGS";

    public static final String LAUNCH_VPN_FRAGMENT_TAG = "launchVpnFragment";
    @Presenter
    MainPresenter presenter;

    View upgradeLabel;
    View userInfo;
    TextView userEmail;
    TextView payPlan;
    TextView limits;
    View headerView;

    private LaunchVpnFragment launchVpnFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (showLoginIfNeeded())
            return;

        initToolbarAndDrawer();

        if (savedInstanceState == null) {
            launchVpnFragment = LaunchVpnFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root, launchVpnFragment, LAUNCH_VPN_FRAGMENT_TAG)
                    .commit();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (launchVpnFragment != null) {
            getSupportFragmentManager().putFragment(outState, LAUNCH_VPN_FRAGMENT_TAG, launchVpnFragment);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        instantiateFragments(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        User.PayPlan payPlan = UserManager.getInstance().getCurrentUser().getPlan();
        if (payPlan.getPlanExpired() <= 0) {
            presenter.showTrialExpired(this);
            return;
        }
        updateNavHeader();
        presenter.registerPaymentReceiver(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unregisterPaymentReceiver(this);
    }

    private void instantiateFragments(Bundle inState) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        if (inState != null) {
            launchVpnFragment = (LaunchVpnFragment) manager.getFragment(inState, LAUNCH_VPN_FRAGMENT_TAG);
        } else {
            launchVpnFragment = LaunchVpnFragment.newInstance();
            transaction.add(R.id.root, launchVpnFragment, LAUNCH_VPN_FRAGMENT_TAG);
            transaction.commit();
        }
    }

    private void initToolbarAndDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        initNavHeader();

        getSupportActionBar().setTitle("");
    }

    private void initNavHeader() {

        upgradeLabel = headerView.findViewById(R.id.upgrade_btn);
        userInfo = headerView.findViewById(R.id.user_info);
        userEmail = (TextView) headerView.findViewById(R.id.user_email);
        payPlan = (TextView) headerView.findViewById(R.id.pay_plan);
        limits = (TextView) headerView.findViewById(R.id.limits);
        upgradeLabel.setOnClickListener(this);

        updateNavHeader();
    }

    @Override
    public void updateNavHeader() {
        User user = UserManager.getInstance().getCurrentUser();
        boolean isTrial = user.getPlan().isTrial();

        userEmail.setText(user.getEmail());
        payPlan.setText(isTrial ? "free account" : "premium account");
        userInfo.setBackgroundResource(isTrial ? R.color.bg_graphic : R.drawable.side_nav_bar);
        upgradeLabel.setVisibility(!isTrial ? View.GONE : View.VISIBLE);
        limits.setText(getFormatLimitString(user.getBandwidth().get("limit")));
        //change nav header size
        int headerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                !isTrial ? 160 : 208, //TODO magic numbers
                getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) headerView.getLayoutParams();
        params.height = headerHeight;
        headerView.setLayoutParams(params);
    }

    private String getFormatLimitString(String limitStr) {
        float limit = Float.parseFloat(limitStr);
        if (limit > 1000) {
            int gb = (int)(limit / 1000);
            return String.format("%d Gb", gb);
        } else {
            return String.format("%d Mb", (int)limit);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            presenter.openSettings(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_change_password) {
            presenter.changePassword(this);
            trackAction(Constants.ANALYTICS_ACTION_CHANGE_PASS);
        } else if (id == R.id.nav_rate) {
            presenter.rateOutService(this);
            trackAction(Constants.ANALYTICS_ACTION_RATE_APP);
        } else if (id == R.id.nav_settings) {
            presenter.openSettings(this);
        } else if (id == R.id.nav_sign_out) {
            presenter.signOut(this);
            trackAction(Constants.ANALYTICS_ACTION_SIGN_OUT);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean showLoginIfNeeded() {
        if (!UserManager.getInstance().isSingedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upgrade_btn:
                presenter.upgradeToPremium(this);
                break;
        }
    }

}

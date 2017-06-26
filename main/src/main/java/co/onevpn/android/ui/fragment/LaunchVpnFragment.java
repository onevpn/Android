package co.onevpn.android.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import co.onevpn.android.Constants;
import co.onevpn.android.OneVpnApp;
import co.onevpn.android.R;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.model.VpnUiStateManager;
import co.onevpn.android.ui.activity.BaseActivity;
import co.onevpn.android.ui.contract.LaunchVpnContract;
import co.onevpn.android.ui.presenter.LaunchVpnPresenter;
import de.blinkt.openvpn.core.ConnectionStatus;
import easymvp.annotation.FragmentView;
import easymvp.annotation.Presenter;


@FragmentView(presenter = LaunchVpnPresenter.class)
public class LaunchVpnFragment extends BaseFragment implements View.OnClickListener, LaunchVpnContract {
    private static final String PREF_UI_VPN_STATE = "PREF_LAUNCH_BTN_IMAGE";
    private static final String PREF_IP_LABEL = "PREF_IP_LABEL";

    @Presenter
    LaunchVpnPresenter presenter;
    private ImageView launchBtn;
    private ImageView countryFlag;
    private TextView countryTitle;
    private TextView connectionLabel;
    private TextView ipLabel;
    private View root;
    ProgressDialog progress;

    private VpnUiStateManager.State currentState;

    private Snackbar snackbar;

    public LaunchVpnFragment() {
        super();

        screenName = "VPN";
    }

    public static LaunchVpnFragment newInstance() {
        LaunchVpnFragment fragment = new LaunchVpnFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        trackHit();
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.initVpnListeners();
        VpnUiStateManager.getInstance().setChangeStateListener(new VpnUiStateManager.VpnStateChangeListener() {
            @Override
            public void onChange(VpnUiStateManager.State state, String ipAdress) {
                updateVpnState(state, ipAdress);
            }
        });

        initSelectedServer();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentState = (VpnUiStateManager.State) savedInstanceState.get(PREF_UI_VPN_STATE);
            String ipAddress = savedInstanceState.getString(PREF_IP_LABEL);

            updateVpnState(currentState, ipAddress);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PREF_IP_LABEL, ipLabel.getText().toString());
        outState.putSerializable(PREF_UI_VPN_STATE, currentState);
    }

    private void updateVpnState(VpnUiStateManager.State state, String ipAddress) {
        if (isDetached() || isRemoving())
            return;

        String status = OneVpnApp.getInstance().getString(state.statusRes);
        int unicodeUnlocked = 0x1F513;
        int unicodeLocked = 0x1F510;
        int unicodeNoEntry = 0x26D4;
        int unicodeChecked = 0x2705;
        switch (state) {
            case NotReachable:
                status = getEmojiByUnicode(unicodeNoEntry) + " " + status;
                break;
            case SecuredCell:
                status = getEmojiByUnicode(unicodeLocked)
                        + getEmojiByUnicode(unicodeChecked) + " " + status;
                break;
            case UnsecuredCell:
                status = getEmojiByUnicode(unicodeUnlocked)
                        + getEmojiByUnicode(unicodeNoEntry) + " " + status;
                break;
            case SecuredWifi:
                status = getEmojiByUnicode(unicodeLocked)
                        + getEmojiByUnicode(unicodeChecked) + " " + status;
                break;
            case UnsecuredWifi:
                status = getEmojiByUnicode(unicodeUnlocked)
                        + getEmojiByUnicode(unicodeNoEntry) + " " + status;
                break;
            case SecuredTrustedWifi:
                status = getEmojiByUnicode(unicodeLocked)
                        + getEmojiByUnicode(unicodeChecked) + " " + status;
                break;
            case UnsecuredTrustedWifi:
                status = getEmojiByUnicode(unicodeUnlocked)
                        + getEmojiByUnicode(unicodeNoEntry) + " " + status;
                break;
        }

        connectionLabel.setText(status);
        launchBtn.setImageResource(state.iconRes);
        if (TextUtils.isEmpty(ipAddress)) {
            ipLabel.setText(R.string.vpn_refresh_ip);
        } else {
            ipLabel.setText(ipAddress);
        }

        currentState = state;
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    @Override
    public void onPause() {
        super.onPause();

        presenter.clearVpnListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_launch_vpn, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        connectionLabel = (TextView) view.findViewById(R.id.connection_label);
        ipLabel = (TextView) view.findViewById(R.id.ip_label);
        launchBtn = (ImageView) view.findViewById(R.id.launch_btn);
        countryTitle = (TextView) view.findViewById(R.id.country_title);
        countryFlag = (ImageView) view.findViewById(R.id.country_flag);
        view.findViewById(R.id.choose_server_btn).setOnClickListener(this);
        launchBtn.setOnClickListener(this);
    }

    private void initSelectedServer() {
        User.Server server = UserManager.getSelectedServer();
        if (server != null) {
            countryTitle.setText(server.getCountry());
            Picasso.with(OneVpnApp.getInstance())
                    .load(server.getFlag())
                    .into(countryFlag);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_server_btn:
                if (currentState == null || !currentState.isSecured) {
                    presenter.openServers(getActivity());
                }
                break;
            case R.id.launch_btn:
                presenter.toggleVPN((BaseActivity) getActivity());
                break;
        }
    }

    @Override
    public void updateVpnState(String state, String logMessage, int localizedResId, ConnectionStatus level) {
        if (isRemoving() || isDetached()) {
            return;
        }
        Logger.d("vpn-status", "vpn status: " + level);
        Logger.d("vpn-status", "vpn state: " + state);
        Logger.d("vpn-status", "vpn logMessage: " + logMessage);
        Logger.d("vpn-status", "vpn localized message: " + getString(localizedResId));

        if (level == ConnectionStatus.LEVEL_CONNECTED) {
            String[] parts = logMessage.split(",");
            if (parts.length >= 3)
                VpnUiStateManager.getInstance().updateIpAddress(parts[2]);
        } else if (level == ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET
                || level == ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED
                || level == ConnectionStatus.LEVEL_START) {
            VpnUiStateManager.getInstance().updateIpAddress(null);
        }
    }

    @Override
    public void showWifiSuggestion(final String wifi) {
        snackbar = Snackbar
                .make(root, R.string.add_wifi_to_exception, Snackbar.LENGTH_LONG)
                .setAction(R.string.add, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<String> wifiList = OneVpnPreferences.getWhiteWifiList();
                        wifiList.add(wifi);
                        OneVpnPreferences.setWhiteWifiList(wifiList);
                        snackbar.dismiss();
                    }
                });
        snackbar.show();
    }

    @Override
    public void showProgress(boolean show) {
        if (show) {
            progress = new ProgressDialog(getActivity());
            progress.setTitle(getString(R.string.please_wait));
            progress.setMessage(getString(R.string.please_wait_progress_descr));
            progress.setCancelable(false);
            progress.setIndeterminate(true);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        } else {
            if (isRemoving() || isDetached())
                return;
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
        }
    }


}

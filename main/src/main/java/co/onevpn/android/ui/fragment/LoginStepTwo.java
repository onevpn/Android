package co.onevpn.android.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.w3c.dom.Text;

import co.onevpn.android.Constants;
import co.onevpn.android.R;
import co.onevpn.android.ui.LoginEditText;
import co.onevpn.android.ui.activity.MainActivity;
import co.onevpn.android.ui.contract.LoginTwoStepContract;
import co.onevpn.android.ui.presenter.LoginStepTwoPresenter;
import easymvp.annotation.FragmentView;
import easymvp.annotation.Presenter;

@FragmentView(presenter = LoginStepTwoPresenter.class)
public class LoginStepTwo  extends BaseFragment
        implements View.OnClickListener, LoginTwoStepContract {

    public interface OnTextChangedListener {
        void onChanged(String text);
    }

    @Presenter
    LoginStepTwoPresenter presenter;

    private LoginEditText email;
    private LoginEditText password;
    private View root;
    private View progress;
    private TextView signInBtn;
    private View firstLaunchProgress;
    private TextView firstLaunchProgressText;

    public LoginStepTwo() {
        super();
        screenName = "Signin";
    }

    public static LoginStepTwo newInstance() {
        LoginStepTwo fragment = new LoginStepTwo();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_login_step_two, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email = (LoginEditText) view.findViewById(R.id.email);
        password = (LoginEditText) view.findViewById(R.id.password);
        progress = view.findViewById(R.id.progress);
        signInBtn = (TextView) view.findViewById(R.id.singin_btn);
        firstLaunchProgress = view.findViewById(R.id.first_launch_progress);
        firstLaunchProgressText = (TextView) view.findViewById(R.id.first_launch_progress_text);
        view.findViewById(R.id.singup_btn).setOnClickListener(this);
        view.findViewById(R.id.forgot).setOnClickListener(this);

        email.setUp(R.string.login_email_label, R.string.login_email_label2);
        password.setUp(R.string.login_password_label, R.string.login_password_label2, true);
        signInBtn.setOnClickListener(this);

        OnTextChangedListener listener = new OnTextChangedListener() {
            @Override
            public void onChanged(String text) {
                if (!TextUtils.isEmpty(email.getText()) && !TextUtils.isEmpty(password.getText())) {
                    signInBtn.setBackgroundResource(R.drawable.bg_rect_round_green_64);
                    signInBtn.setTextColor(getResources().getColor(R.color.white));
                } else {
                    signInBtn.setBackgroundResource(R.drawable.bg_rect_round_64);
                    signInBtn.setTextColor(getResources().getColor(R.color.login_text_color));
                }
            }
        };

        email.setOnTextChanged(listener);
        password.setOnTextChanged(listener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.singin_btn:
                if (!TextUtils.isEmpty(email.getText()) && !TextUtils.isEmpty(password.getText())) {
                    presenter.signIn(email.getText(), password.getText());
                    hideKeyboard();
                    trackAction(Constants.ANALYTICS_ACTION_SIGNIN);
                }
                break;
            case R.id.singup_btn:
                presenter.signUp(getActivity());
                trackAction(Constants.ANALYTICS_ACTION_SIGNUP);
                break;
            case R.id.forgot:
                presenter.forgot(getActivity());
                trackAction(Constants.ANALYTICS_ACTION_FORGOT);
                break;
        }
    }

    @Override
    public void showProgress(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        signInBtn.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showFirstLaunchProgress(boolean show) {
        firstLaunchProgress.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            presenter.setupFirstLaunchStatuses();
        }
    }

    @Override
    public void setFirstLaunchProgressText(String text) {
        firstLaunchProgressText.setText(text);
    }

    @Override
    public void showError(String error) {
        Snackbar
                .make(root, error, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void processSuccess() {
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

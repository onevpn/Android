package co.onevpn.android.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.onevpn.android.Constants;
import co.onevpn.android.R;
import co.onevpn.android.ui.contract.LoginOneStepContract;
import co.onevpn.android.ui.presenter.LoginStepOnePresenter;
import easymvp.annotation.FragmentView;
import easymvp.annotation.Presenter;

@FragmentView(presenter = LoginStepOnePresenter.class)
public class LoginStepOne extends BaseFragment implements View.OnClickListener, LoginOneStepContract {
    public interface LoginStepInteractor {
        void moveToStepTwo();
    }

    @Presenter
    LoginStepOnePresenter presenter;
    private LoginStepInteractor loginStepInteractor;

    public LoginStepOne() {
        super();

        screenName = "Account";
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof LoginStepInteractor) {
            loginStepInteractor = (LoginStepInteractor) context;
        } else {
            throw new IllegalStateException("Activity should implements LoginStepInteractor");
        }

        trackHit();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        loginStepInteractor = null;
    }

    public static LoginStepOne newInstance() {
        LoginStepOne fragment = new LoginStepOne();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_step_one, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_yes).setOnClickListener(this);
        view.findViewById(R.id.btn_no).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_no) {
            presenter.clickHaventBtn(getActivity());
            trackAction(Constants.ANALYTICS_ACTION_SIGNUP);
        } else if (v.getId() == R.id.btn_yes) {
            presenter.clickHaveBtn();
        }
    }

    @Override
    public void showLoginPanel() {
        loginStepInteractor.moveToStepTwo();
    }
}

package co.onevpn.android.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.onevpn.android.R;
import co.onevpn.android.ui.fragment.LoginStepTwo;

/**
 * Created by sergeygorun on 27/03/2017.
 */

public class LoginEditText extends RelativeLayout {
    private EditText text;
    private View bottomLine;
    private TextView additionalHint;
    private LoginStepTwo.OnTextChangedListener onTextChangedListener;

    public LoginEditText(Context context) {
        super(context);
    }

    public LoginEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoginEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        text = (EditText) findViewById(R.id.text);
        bottomLine = findViewById(R.id.bottom_line);
        additionalHint = (TextView) findViewById(R.id.hint);

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    bottomLine.setBackgroundColor(getResources().getColor(R.color.login_green));
                    additionalHint.setVisibility(View.VISIBLE);
                } else {
                    additionalHint.setVisibility(View.INVISIBLE);
                    bottomLine.setBackgroundColor(getResources().getColor(R.color.login_bottom_line));
                }
                if (onTextChangedListener != null)
                    onTextChangedListener.onChanged(s == null ? null : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public String getText() {
        return text.getText().toString();
    }

    public void setUp(int hint1, int hint2) {
        setUp(hint1, hint2, false);
    }

    public void setUp(int hint1, int hint2, boolean isPassword) {
        text.setHint(hint1);
        additionalHint.setText(hint2);
        if (isPassword) {
            text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void setOnTextChanged(LoginStepTwo.OnTextChangedListener onTextChanged) {
        this.onTextChangedListener = onTextChanged;
    }
}

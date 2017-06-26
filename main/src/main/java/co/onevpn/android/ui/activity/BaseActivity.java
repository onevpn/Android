package co.onevpn.android.ui.activity;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;



public class BaseActivity extends AppCompatActivity {
    protected String screenName = getClass().getSimpleName();

    protected <T extends View> T fbid(@IdRes int id) {
        return (T) findViewById(id);
    }

    protected void trackHit() {
        //track hit here
    }

    public void trackAction(String action) {
        //track action here
    }
}

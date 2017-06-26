package co.onevpn.android.ui.fragment;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.View;

public class BaseFragment extends Fragment {
    protected String screenName = getClass().getSimpleName();

    protected <T extends View> T fbid(View v, @IdRes int id) {
        return (T) v.findViewById(id);
    }

    protected void trackHit() {
        //track hit here
    }

    protected void trackAction(String action) {
        //track action here
    }
}

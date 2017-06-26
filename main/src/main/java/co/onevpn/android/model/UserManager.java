package co.onevpn.android.model;

import net.grandcentrix.tray.AppPreferences;

import co.onevpn.android.OneVpnApp;
import co.onevpn.android.utils.CryptoUtils;

public class UserManager {
    public static final String TOKEN = "Fuck_The_Internet";

    private static UserManager ourInstance = new UserManager();

    private User currentUser;

    public static UserManager getInstance() {
        return ourInstance;
    }

    private UserManager() {
        if (isSingedIn())
            currentUser = OneVpnPreferences.getCurrentUser();
    }

    public boolean isSingedIn() {
        return OneVpnPreferences.isSingedIn();
    }

    public void signIn(User user) {
        OneVpnPreferences.setUserSignIn(true, user);
        currentUser = user;
    }

    public synchronized void update(User user) {
        OneVpnPreferences.saveUser(user);
    }

    public void signOut() {
        OneVpnPreferences.setUserSignIn(false, null);
        ProfileAssembler.clearConfigSample();
        new OneVpnProfileManager(currentUser).clearConfigs();
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public static String getHashedPassword(String password) {
        return CryptoUtils.md5(password +  UserManager.TOKEN);
    }

    public static User.Server getSelectedServer() {
        if (getInstance().getCurrentUser() != null) {
            User.Server server = null;
            for (User.Server s:  UserManager.getInstance().getCurrentUser().getServers()) {
                if (s.isSelected()) {
                    server = s;
                    break;
                }
            }

            return server;
        }

        return null;
    }
}

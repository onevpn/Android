package co.onevpn.android.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.List;

import co.onevpn.android.R;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.model.UserManager;

public class ConnectionModeDialog extends DialogFragment {
    private int selectedConnectionModeNumber = 0;
    private List<String> connections;

    public ConnectionModeDialog() {
        super();
        loadData();
    }

    private void loadData() {
        connections = UserManager.getInstance().getCurrentUser().getServers().get(0).getProtocol();
        String connMode = OneVpnPreferences.getConnectionMode();
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).equals(connMode)) {
                selectedConnectionModeNumber = i;
            }
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] connCs = connections.toArray(new CharSequence[connections.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(R.string.port)
                .setSingleChoiceItems(connCs, selectedConnectionModeNumber, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedConnectionModeNumber = i;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        OneVpnPreferences.setConnectionMode(
                                connections.get(selectedConnectionModeNumber), true);
                        Logger.d("Set connection mode: " +
                                connections.get(selectedConnectionModeNumber));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
package co.onevpn.android.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import co.onevpn.android.R;
import co.onevpn.android.model.OneVpnPreferences;

/**
 * Created by sergeygorun on 15/12/2016.
 */

public class BlockCountryDialog extends DialogFragment {
    Map<String, Boolean> blockCountries = new HashMap<>();
    private boolean[] blockCountriesArr;
    private CharSequence[] countries;

    public BlockCountryDialog() {
        super();
        loadData();
    }

    private void loadData() {
        blockCountries = OneVpnPreferences.getBlockedCountries();
        blockCountriesArr = new boolean[blockCountries.size()];
        countries = blockCountries.keySet().toArray(new CharSequence[blockCountries.size()]);
        int i = 0;
        for (CharSequence c : countries) {
            blockCountriesArr[i++] = blockCountries.get(c);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(R.string.dialog_block_country)
                .setMultiChoiceItems(countries, blockCountriesArr, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        blockCountriesArr[i] = b;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < countries.length; i++) {
                            String country = (String) countries[i];
                            blockCountries.put(country, blockCountriesArr[i]);
                        }

                        boolean isAllCountriesDisabled = true;
                        for (boolean c : blockCountriesArr) {
                            if (c) {
                                isAllCountriesDisabled = false;
                                break;
                            }
                        }

                        if (!isAllCountriesDisabled)
                            OneVpnPreferences.setBlockedCountries(blockCountries);
                        else
                            Toast.makeText(getActivity(),
                                "You have to check one or more countries", Toast.LENGTH_SHORT).show();
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
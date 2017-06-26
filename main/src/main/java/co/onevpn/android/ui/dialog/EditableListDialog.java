package co.onevpn.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import co.onevpn.android.R;
import co.onevpn.android.log.Logger;
import co.onevpn.android.model.OneVpnPreferences;
import co.onevpn.android.ui.fragment.SettingsFragment;

/**
 * Created by sergeygorun on 15/12/2016.
 */

public class EditableListDialog extends DialogFragment {
    private static final String ARG_ITEMS = "items";
    private static final String ARG_TITLE = "title";

    private DialogAdapter adapter;
    private String title;

    public EditableListDialog() {
        super();
    }

    public static EditableListDialog newInstance(@NonNull List<String> items, @NonNull String title) {
        EditableListDialog dialog = new EditableListDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_ITEMS, new ArrayList<String>(items));
        args.putString(ARG_TITLE, title);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            List<String> items = getArguments().getStringArrayList(ARG_ITEMS);
            adapter = new DialogAdapter(getActivity(), items);
            title = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle(title)
            .setView(renderView())
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (title.toLowerCase().startsWith("autoconnect"))
                        OneVpnPreferences.setAutoconnect(adapter.items);
                    else
                        OneVpnPreferences.setWhiteWifiList(adapter.items);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        return builder.create();
    }

    private View renderView() {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = layoutInflater.inflate(R.layout.dialog_editable_list, null);
        ListView listView = (ListView) root.findViewById(R.id.items);
        final EditText addEdit = (EditText) root.findViewById(R.id.add);
        View addBtn = root.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAdd(addEdit);
            }
        });

        listView.setAdapter(adapter);
        addEdit.setImeActionLabel("Add", KeyEvent.KEYCODE_ENTER);
        addEdit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    handleAdd(addEdit);
                    return true;
                }
                return false;
            }
        });

        return root;
    }

    private void handleAdd(EditText addEdit) {
        String item = addEdit.getText().toString();
        if (item.length() == 0)
            return;
        if (!adapter.items.contains(item)) {
            adapter.add(item);
            addEdit.setText("");
        } else {
            Toast.makeText(getActivity(), R.string.dialog_item_contained_error, Toast.LENGTH_SHORT).show();
        }
    }

    private class DialogAdapter extends ArrayAdapter<String> {
        private List<String> items;
        private LayoutInflater layoutInflater;

        public DialogAdapter(Context context,  List<String> items) {
            super(context, R.layout.view_editable_dialog_item, items);
            this.items = items;
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView title;
            View removeBtn;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.view_editable_dialog_item, null);
                title = (TextView) convertView.findViewById(R.id.title);
                removeBtn = convertView.findViewById(R.id.remove);
            } else {
                title = (TextView) convertView.findViewById(R.id.title);
                removeBtn = convertView.findViewById(R.id.remove);
            }

            String item = getItem(position);
            if (convertView.getTag() == null || !convertView.getTag().equals(item)) {
                title.setText(item);
                removeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeCurrentItem(position);
                    }
                });
                convertView.setTag(item);
            }

            return convertView;
        }

        private void removeCurrentItem(int position) {
            String item = getItem(position);
            items.remove(item);
            notifyDataSetChanged();
        }
    }
}

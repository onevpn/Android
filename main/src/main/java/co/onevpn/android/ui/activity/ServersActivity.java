package co.onevpn.android.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.onevpn.android.Constants;
import co.onevpn.android.OneVpnApp;
import co.onevpn.android.R;
import co.onevpn.android.model.PingManager;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;
import co.onevpn.android.ui.contract.ServersContract;
import co.onevpn.android.ui.presenter.ServersPresenter;
import easymvp.annotation.ActivityView;
import easymvp.annotation.Presenter;


@ActivityView(layout = R.layout.activity_servers, presenter = ServersPresenter.class)
public class ServersActivity extends BaseActivity implements ServersContract {
    @Presenter
    ServersPresenter presenter;
    private ServerAdapter serverAdapter;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initToolbar();
        initRecyclerView();

        screenName = "Servers";
        trackHit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        runPingServiceIfNeeded(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PingManager.getInstance().setProgressResult(null);
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.servers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_reset_ping) {
            runPingServiceIfNeeded(true);
        }

        return super.onOptionsItemSelected(item);
    }

    private void runPingServiceIfNeeded(boolean force) {
        presenter.runPingService(this, new PingManager.OnProgressResult() {
            @Override
            public void onProgress(Pair<User.Server, Integer> progress) {
                for (User.Server server : serverAdapter.servers)
                    if (server.equals(progress.first))
                        server.setPingTime(progress.second);

                serverAdapter.dataSetChanged();
            }
        }, force);
    }

    private void initRecyclerView() {
        RecyclerView serversRecyclerView = fbid(R.id.servers_recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        serversRecyclerView.setLayoutManager(mLayoutManager);

        serverAdapter = new ServerAdapter(UserManager.getInstance().getCurrentUser().getServers());
        serversRecyclerView.setAdapter(serverAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void refreshServerList() {
        serverAdapter.sortItems();
        serverAdapter.dataSetChanged();
    }

    @Override
    public void showProgress(boolean show) {
        if (isDestroyed() || isFinishing())
            return;

        if (show) {
            progress = ProgressDialog.show(this, getString(R.string.please_wait), getString(R.string.please_wait_progress_descr));
        } else {
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
        }
    }

    public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ViewHolder> {
        private List<User.Server> servers;
        private boolean favoriteServerHeader = false;
        private boolean selectedServerHeader = false;
        private boolean otherServerHeader = false;


        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView header;
            private TextView title;
            private View pingBox;
            private TextView pingTime;
            private ImageView star;
            private ImageView flag;
            private View root;

            public ViewHolder(View v) {
                super(v);
                header = (TextView) v.findViewById(R.id.header);
                title = (TextView) v.findViewById(R.id.title);
                pingBox = v.findViewById(R.id.ping_box);
                pingTime = (TextView) v.findViewById(R.id.ping_time);
                flag = (ImageView) v.findViewById(R.id.flag);
                star = (ImageView) v.findViewById(R.id.icon_star);
                root = v.findViewById(R.id.root);
            }

            void bindView(final User.Server server) {
                showHeaderIfNeeded(server);
                title.setText(server.getName());
                pingTime.setText(String.valueOf(server.getPingTime()));
                if (server.getPingTime() < 250) {
                    pingBox.setBackgroundColor(OneVpnApp.getInstance().getResources().getColor(R.color.bg_item_green));
                } else if (server.getPingTime() < 600) {
                    pingBox.setBackgroundColor(OneVpnApp.getInstance().getResources().getColor(R.color.bg_item_yellow));
                } else {
                    pingBox.setBackgroundColor(OneVpnApp.getInstance().getResources().getColor(R.color.bg_item_red));
                }

                star.setImageResource(server.isFavorite() ? R.drawable.favorite : R.drawable.favorite_no);
                star.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.toggleServerFavorite(server);
                        trackAction(Constants.ANALYTICS_ACTION_CHOOSE_SERVER);
                    }
                });

                Picasso.with(OneVpnApp.getInstance())
                        .load(server.getFlag())
                        .into(flag);

                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.toggleServerSelected(ServersActivity.this, server);
                    }
                });
            }

            private void showHeaderIfNeeded( final User.Server server) {
                header.setVisibility(View. VISIBLE);
                if (server.isSelected() && !selectedServerHeader) {
                    header.setText("Selected server");
                    selectedServerHeader = true;
                } else if (server.isFavorite() && !favoriteServerHeader) {
                    header.setText("Favorite");
                    favoriteServerHeader = true;
                } else if (!otherServerHeader) {
                    header.setText("Servers");
                    otherServerHeader = true;
                } else {
                    header.setVisibility(View.GONE);
                }
            }
        }

        ServerAdapter(List<User.Server> servers) {
            this.servers = servers;
            sortItems();
        }

        void sortItems() {
            Collections.sort(servers, new Comparator<User.Server>() {
                @Override
                public int compare(User.Server s1, User.Server s2) {
                if (s1.isSelected()) {
                    return -1;
                } else if (s2.isSelected()) {
                    return 1;
                } else if (s1.isFavorite() && s2.isFavorite() || !s1.isFavorite() && !s2.isFavorite()) {
                    return 0;
                } else if (s1.isFavorite() && !s2.isFavorite()) {
                    return -1;
                } else if (!s1.isFavorite() && s2.isFavorite()) {
                    return 1;
                }

                return 0;
                }
            });
        }

        void dataSetChanged() {
            favoriteServerHeader = false;
            selectedServerHeader = false;
            otherServerHeader = false;
            notifyDataSetChanged();
        }

        @Override
        public ServerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_server_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindView(servers.get(position));
        }

        @Override
        public int getItemCount() {
            return servers.size();
        }
    }

}

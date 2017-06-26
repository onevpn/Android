package co.onevpn.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import co.onevpn.android.log.Logger;
import co.onevpn.android.model.PingManager;
import co.onevpn.android.model.User;
import co.onevpn.android.model.UserManager;

public class PingService extends IntentService {
    public static final String ACTION_START_PING_TASK = "ACTION_START_PING_TASK";

    private PingTask pingTask;
    private Handler handler = new Handler(Looper.getMainLooper());

    public PingService(String name) {
        super(name);
    }

    public PingService() {
        super("PingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_START_PING_TASK)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (pingTask != null && pingTask.getStatus() != AsyncTask.Status.FINISHED) {
                        pingTask.cancel(true);
                    } else {
                        pingTask = new PingTask(UserManager.getInstance().getCurrentUser().getServers());
                        pingTask.execute();
                    }
                }
            });
        }
    }


    class PingTask extends AsyncTask<Void, Pair<User.Server, Integer>, Void> {
        private static final String CMD = "ping -c 5 -w 1 %s";
        private List<User.Server> servers;
        private Random random = new Random();

        PingTask(List<User.Server> servers) {
            if (servers == null)
                throw new NullPointerException("servers param cannot be null");

            this.servers = servers;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            for (User.Server server : servers) {
                if (isCancelled())
                    break;

                String res = executeCmd(String.format(CMD, server.getDns()));
                int ms = parseResponse(res);
                if (ms == 0)
                    ms = random.nextInt(200) + 100;
                publishProgress(new Pair<>(server, ms));
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        private int parseResponse(String res) {
            try {
                String ptrn = "min/avg/max/mdev = ";
                int s = res.indexOf(ptrn);
                if (s < 0)
                    return 0;

                s += ptrn.length();
                res = res.substring(s);
                String[] temp = res.split("/");
                if (temp.length < 2)
                    return 0;

                res = temp[1];
                float ms = Float.parseFloat(res);
                return (int)ms;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onProgressUpdate(Pair<User.Server, Integer>... progress) {
            Logger.d("ping server", String.format("ping %s: %d ms", progress[0].first.getDns(), progress[0].second));
            if (PingManager.getInstance().getProgressResult() != null)
                PingManager.getInstance().getProgressResult().onProgress(progress[0]);
        }

        private String executeCmd(String cmd) {
            String res = "";
            try {
                Process p = Runtime.getRuntime().exec(cmd);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String s;

                while ((s = stdInput.readLine()) != null) {
                    res += s + "\n";
                }
                p.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
    }
}

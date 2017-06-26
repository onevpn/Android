/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import co.onevpn.android.log.Logger;

/**
 * Created by arne on 09.11.16.
 */

public class StatusListener  {
    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Logger.d("OpenVPNStatusService connected");

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            IServiceStatus serviceStatus = IServiceStatus.Stub.asInterface(service);
            try {
                /* Check if this a local service ... */
                if (service.queryLocalInterface("de.blinkt.openvpn.core.IServiceStatus") == null) {
                    VpnStatus.setConnectedVPNProfile(serviceStatus.getLastConnectedVPN());
                    serviceStatus.registerStatusCallback(mCallback);
                    Logger.d("status callback registred");
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

    public void init(Context c)
    {

        Intent intent = new Intent(c, OpenVPNStatusService.class);
        intent.setAction(OpenVPNService.START_SERVICE);

        c.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }


    private IStatusCallbacks mCallback = new IStatusCallbacks.Stub()

    {
        @Override
        public void newLogItem(LogItem item) throws RemoteException {
            VpnStatus.newLogItem(item);
        }

        @Override
        public void updateStateString(String state, String msg, int resid, ConnectionStatus
                level) throws RemoteException {
            VpnStatus.updateStateString(state, msg, resid, level);

            Logger.d("get new connection state: " + state);
            Logger.d("get new connection status: " + level);
        }

        @Override
        public void updateByteCount(long inBytes, long outBytes) throws RemoteException {
            VpnStatus.updateByteCount(inBytes, outBytes);
        }

        @Override
        public void connectedVPN(String uuid) throws RemoteException {
            VpnStatus.setConnectedVPNProfile(uuid);

            Logger.d("connected vpn profile: " + uuid);
        }
    };

}

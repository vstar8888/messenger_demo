package ru.demo.messenger.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by igor on 30.11.15.
 */
public class ConnectionChangedService extends BroadcastReceiver {

    private static boolean registered;

    public static void registerConnectionChangedReceiver (Context context) {
        if (!registered) {
            registered = true;
            context.registerReceiver(new ConnectionChangedService(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to wifi or mobile network
                if (ConnectionService.signalRState.equals(ConnectionService.STATE_DISCONNECTED)) {
                    ConnectionService.getInstance().init();
                }
            }
        } else {
            // not connected to the internet
        }
    }


}

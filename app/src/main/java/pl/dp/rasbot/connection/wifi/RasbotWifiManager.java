package pl.dp.rasbot.connection.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.ennova.rxwifi.RxWifi;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by Project4You S.C. on 08.08.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class RasbotWifiManager {

    public static final String TAG = "RasbotWifiManager";

    private Context context;

    private WifiManager wifiManager;

    private WifiConnectionReceiver wifiConnectionReceiver;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();


//    private static final String RASBOT_NETWORK_NAME = "rasbot";
//    private static final String RASBOT_NETWORK_PASWORD = "rasbot123";

    private static final String RASBOT_NETWORK_NAME = "PGS Software WiFi";
    //                                                 PGS Software WiFi
    private static final String RASBOT_NETWORK_PASWORD = "qla3Iad9ousla";

    private List<WifiConnectionListener> connectionListenerList = new ArrayList<>();

    public RasbotWifiManager(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        wifiConnectionReceiver = new WifiConnectionReceiver(connectionListenerList, wifiManager);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(wifiConnectionReceiver, filter);
    }

    public void addConectionListener(WifiConnectionListener connectionListener){
        connectionListenerList.add(connectionListener);
    }

    public void removeConectionListener(WifiConnectionListener connectionListener){
        connectionListenerList.remove(connectionListener);
    }

    public void searchForRasbotNetwork() {

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }


        compositeSubscription.add(RxWifi.from(context)
                .filter(result -> result.SSID.equals(RASBOT_NETWORK_NAME))
                .toList()
                .doOnNext(list -> {
                    if (list.isEmpty() && connectionListenerList != null) {
                        for(WifiConnectionListener wcl: connectionListenerList){
                            wcl.networkNotFound();
                        }
                    }
                })
                .map(resultsList -> Collections.max(resultsList, (scanResult, t1) -> new Integer(scanResult.level).compareTo(t1.level)))
                .subscribe(scanResult -> {
                    for(WifiConnectionListener wcl: connectionListenerList){
                        wcl.networkFound();
                    }

                    wifiManager.disconnect();
                    wifiManager.enableNetwork(getWifiConfiguration(scanResult, wifiManager.getConfiguredNetworks()).networkId, true);
                    wifiManager.reconnect();

                }, Throwable::printStackTrace));
    }


    public void release() {
        context.unregisterReceiver(wifiConnectionReceiver);
        compositeSubscription.clear();
    }

    public boolean isRasbotConnection() {
        String ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        Timber.d("RasbotWifiManager:isRasbotConnection: ssid: %s", ssid);

        if (ssid.equals(RASBOT_NETWORK_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    @NonNull
    private WifiConfiguration getWifiConfiguration(ScanResult scanResult, List<WifiConfiguration> wifiConfigurationList) {
        WifiConfiguration rasbotNetworkConfiguration = new WifiConfiguration();
        boolean wifiConfExist = false;

        for (WifiConfiguration wc : wifiConfigurationList) {
            try {

                String wifiName = wc.SSID.replace("\"", "");
                if (wifiName.equals(scanResult.SSID)) {
                    wifiConfExist = true;
                    rasbotNetworkConfiguration = wc;
                    break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (!wifiConfExist) {

            rasbotNetworkConfiguration.SSID = String.format("\"%s\"", scanResult.SSID);
            rasbotNetworkConfiguration.preSharedKey = String.format("\"%s\"", RASBOT_NETWORK_PASWORD);
            rasbotNetworkConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            rasbotNetworkConfiguration.status = WifiConfiguration.Status.ENABLED;

            rasbotNetworkConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            rasbotNetworkConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            rasbotNetworkConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            rasbotNetworkConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            rasbotNetworkConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            rasbotNetworkConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

            try {
                WifiConfUtils.setIpAssignment("STATIC", rasbotNetworkConfiguration);
                WifiConfUtils.setIpAddress(InetAddress.getByName("192.168.2.4"), 24, rasbotNetworkConfiguration);
                WifiConfUtils.setGateway(InetAddress.getByName("192.168.2.1"), rasbotNetworkConfiguration);
            } catch (Exception e) {
                e.printStackTrace();
            }
            rasbotNetworkConfiguration.networkId = wifiManager.addNetwork(rasbotNetworkConfiguration);

        } else {
            Log.d(TAG, "Sieć rasbot istnieje: " + rasbotNetworkConfiguration.networkId);
        }
        return rasbotNetworkConfiguration;
    }


    public static class WifiConnectionReceiver extends BroadcastReceiver {

        private List<WifiConnectionListener> connectionListenerList;

        private WifiManager wifiManager;

        public WifiConnectionReceiver(List<WifiConnectionListener> connectionListener, WifiManager wifiManager) {
            this.connectionListenerList = connectionListener;
            this.wifiManager = wifiManager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                String ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
                Timber.d("WifiConnectionReceiver:onReceive: SSID: " + ssid);
                if (networkInfo.isConnected() && ssid.equals(RASBOT_NETWORK_NAME)) {
                    for(WifiConnectionListener wcl: connectionListenerList){
                        wcl.connected();
                    }
                }
            } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                        !networkInfo.isConnected()) {
                    for(WifiConnectionListener wcl: connectionListenerList){
                        wcl.disconnected();
                    }

                }
            }

        }
    }

}


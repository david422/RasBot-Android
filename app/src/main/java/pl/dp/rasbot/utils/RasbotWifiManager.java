package pl.dp.rasbot.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.net.InetAddress;
import java.util.List;

import pl.dp.rasbot.MainActivity;
import pl.dp.rasbot.wifi.WifiConfUtils;

/**
 * Created by Project4You S.C. on 08.08.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class RasbotWifiManager{

    public static final String TAG = "RasbotWifiManager";

    private Context context;

    private WifiManager wifiManager;

    private WifiReceiver receiverWifi;
    private WifiConnectionReceiver wifiConnectionReceiver = new WifiConnectionReceiver();

    private Handler handler = new Handler();

    private boolean isScanning = false;

    private static final String RASBOT_NETWORK_NAME = "rasbot";
    private static final String RASBOT_NETWORK_PASWORD = "rasbot123";

    public RasbotWifiManager(Context context) {
        this.context = context;

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);



        receiverWifi = new WifiReceiver();



    }

    public void searchForRasbotNetwork(){

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            return;
        }

        wifiManager.startScan();
        isScanning = true;

        if (!isScanning)
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isScanning){
                        wifiManager.startScan();
                        Log.d(TAG, "Scanning...  Discovered device: " + wifiManager.getScanResults().size());
                        handler.removeCallbacks(null);
                        handler.postDelayed(this, 1000);
                    }
                }
            }, 1000);
    }

    public void stopScanning(){
        isScanning = false;
    }

    public void onResume(){
        context.registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        context.registerReceiver(wifiConnectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    public void onPause(){
        context.unregisterReceiver(receiverWifi);
        context.unregisterReceiver(wifiConnectionReceiver);
    }

    public boolean isRasbotConnection() {
        String ssid = wifiManager.getConnectionInfo().getSSID().replace("\"","");
        if(ssid.equals(RASBOT_NETWORK_NAME)){
//            BusProvider.getInstance().post(MainActivity.ACTION_NETWORK_CONNECTED);
//            handler.sendEmptyMessage(MainActivity.ACTION_NETWORK_CONNECTED);
            return true;
        }else{
            return false;
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    public class WifiReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> wifiList = wifiManager.getScanResults();

            Log.d(TAG, "intent Action: " + intent.getAction());





            if (isRasbotConnection())
                return;

            for (ScanResult scanResult: wifiList){
                if (scanResult.SSID.equals(RASBOT_NETWORK_NAME)){

//                    BusProvider.getInstance().post(MainActivity.ACTION_RASBOT_DISCOVERED);
//                    handler.sendEmptyMessage(MainActivity.ACTION_RASBOT_DISCOVERED);
                    WifiConfiguration rasbotNetworkConfiguration = new WifiConfiguration();



                    wifiManager.disconnect();

                    List<WifiConfiguration> wifiConfigurationList= wifiManager.getConfiguredNetworks();

                    if (wifiConfigurationList == null)
                        return;

                    boolean wifiConfExist = false;
                    for (WifiConfiguration wc: wifiConfigurationList){
                        try {
                            Log.d(TAG, "network: " + wc.SSID + ": " + wc.networkId);
                            String wifiName = wc.SSID.replace("\"", "");
                            if (wifiName.equals(scanResult.SSID)) {
                                wifiConfExist = true;
                                rasbotNetworkConfiguration = wc;
                                break;
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }

                    if (!wifiConfExist){

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

                    }else{
                        Log.d(TAG, "Sieć rasbot istnieje: " + rasbotNetworkConfiguration.networkId);
                    }

                    wifiManager.enableNetwork(rasbotNetworkConfiguration.networkId, true);
                    wifiManager.reconnect();
                }
            }
        }
    }

    public class WifiConnectionReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();

            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, "Have Wifi Connection");
                if (isRasbotConnection()) {
//                    BusProvider.getInstance().post(MainActivity.ACTION_NETWORK_CONNECTED);
//                    handler.sendEmptyMessage(MainActivity.ACTION_NETWORK_CONNECTED);
                    isScanning = false;
                } else {
                    searchForRasbotNetwork();
                }

            }else{
                    Log.d(TAG, "Don't have Wifi Connection");
                isScanning = false;
//                    handler.sendEmptyMessage(MainActivity.ACTION_NETWORK_DISCONNECTED);
                }
        }
    }


}


package pl.dp.rasbot;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import pl.dp.rasbot.connection.MessageManager;
import pl.dp.rasbot.connection.MessageCallback;
import pl.dp.rasbot.connection.PingCallback;
import pl.dp.rasbot.connection.PingManager;
import pl.dp.rasbot.connection.wifi.WifiConnectionListener;
import pl.dp.rasbot.event.ConnectionStatusEvent;
import pl.dp.rasbot.event.MessageEvent;
import pl.dp.rasbot.message.Message;
import pl.dp.rasbot.message.ReceivedMessage;
import pl.dp.rasbot.utils.BusProvider;
import pl.dp.rasbot.connection.wifi.RasbotWifiManager;
import timber.log.Timber;

/**
 * Created by Project4You S.C. on 29.08.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class ConnectionService extends Service implements MessageCallback {

    private final IBinder mbinder = new LocalBinder();

    private MessageManager messageManager;
    private PingManager pingManager;

    private RasbotWifiManager wifiManager;

    private static final String host = "10.10.32.92";
    //    private static final String host = "192.168.2.1";
    private static final int PING_PORT = 4334;
    private static final int MESSAGE_PORT = 4333;
    private WifiHandler wifiHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    public boolean isConnected() {
        return messageManager.isConnected();
    }

    public void sendMessage(Message data) {
        messageManager.sendMessage(data);
    }

    @Override
    public void onMessageReceived(ReceivedMessage message) {
        BusProvider.getInstance().post(new MessageEvent(message));
    }

    public class LocalBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        messageManager = new MessageManager(host, MESSAGE_PORT);
        messageManager.setMessageCallback(this);

        pingManager = new PingManager(host, PING_PORT);
        PingHandler pingHandler = new PingHandler(messageManager, this);
        pingManager.setPingCallback(pingHandler);


        Timber.d("ConnectionService:onCreate: init service");
        wifiHandler = new WifiHandler(this, pingManager);
        wifiManager = new RasbotWifiManager(this, wifiHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wifiManager.release();
        Timber.d("ConnectionService:onDestroy: ");
    }

    public void connect() {

        if (!wifiManager.isRasbotConnection()) {
            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.RASBOT_WIFI_NETWORK_SEARCHING));
            wifiManager.searchForRasbotNetwork();
        } else if (wifiManager.isRasbotConnection()) {
            wifiHandler.connected();
        }
    }

    private static class WifiHandler implements WifiConnectionListener {

        private ConnectionService connectionService;
        private PingManager pingManager;

        public WifiHandler(ConnectionService connectionService, PingManager pingManager) {
            this.connectionService = connectionService;
            this.pingManager = pingManager;
        }

        @Override
        public void connected() {
            Timber.d("WifiHandler:connected: ");

            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.RASBOT_WIFI_NETWORK_CONNECTED));
            if (!pingManager.isConnected()) {
                new Handler().postDelayed(() -> pingManager.init(), 1500);
                BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.START_CONNECTING));
            } else if (pingManager.isConnected()) {
                BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.CONNECTION_ESTABLISHED));
            }
        }

        @Override
        public void networkFound() {
            Timber.d("WifiHandler:networkFound: ");
            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.RASBOT_WIFI_NETWORK_FOUND));
        }

        @Override
        public void networkNotFound() {
            Timber.d("WifiHandler:networkNotFound: ");
            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.RASBOT_WIFI_NETWORK_NOT_FOUND));
        }

        @Override
        public void disconnected() {
            Timber.d("WifiHandler:disconnected: ");
            pingManager.release();
            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.RASBOT_WIFI_NETWORK_DISCONNECTED));
            connectionService.stopSelf();
        }
    }

    private static class PingHandler implements PingCallback {

        private MessageManager connectionManager;
        private ConnectionService connectionService;

        public PingHandler(MessageManager connectionManager, ConnectionService connectionService) {
            this.connectionManager = connectionManager;
            this.connectionService = connectionService;
        }

        @Override
        public void connectionEstablished() {
            connectionManager.connect();
            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.CONNECTION_ESTABLISHED));
        }

        @Override
        public void connectionInterrupted() {
            Timber.d("connectionInterrupted: ");
            connectionManager.release();
            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.CONNECTION_INTERRUPTED));
            connectionService.stopSelf();
        }

        @Override
        public void connectionError() {
            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.CONNECTION_ERROR));
            connectionManager.release();
            connectionService.stopSelf();
        }

        @Override
        public void connectionTimeout() {

            connectionManager.release();
            connectionService.stopSelf();
        }
    }
}

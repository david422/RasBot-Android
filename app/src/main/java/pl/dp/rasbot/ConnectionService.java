package pl.dp.rasbot;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import pl.dp.rasbot.connection.ConnectionManager;
import pl.dp.rasbot.connection.MessageCallback;
import pl.dp.rasbot.connection.PingCallback;
import pl.dp.rasbot.connection.PingManager;
import pl.dp.rasbot.event.ConnectionStatusEvent;
import pl.dp.rasbot.event.MessageEvent;
import pl.dp.rasbot.message.Message;
import pl.dp.rasbot.utils.BusProvider;
import timber.log.Timber;

/**
 * Created by Project4You S.C. on 29.08.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class ConnectionService extends Service implements PingCallback, MessageCallback {

    private final IBinder mbinder = new LocalBinder();

    private ConnectionManager connectionManager;
    private PingManager pingManager;

    private static final String host = "10.10.32.92";
    private static final int PING_PORT = 4334;
    private static final int MESSAGE_PORT = 4333;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    public boolean isConnected() {
        return connectionManager.isConnected();
    }

    public void sendMessage(Message data) {
        connectionManager.sendMessage(data);
    }

    @Override
    public void connectionEstablished() {
        Timber.d("connectionEstablished: ");
        connectionManager = new ConnectionManager(host, MESSAGE_PORT);
        connectionManager.setMessageCallback(this);
        connectionManager.connect();

        BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.CONNECTION_ESTABLISHED));
    }

    @Override
    public void connectionInterrupted() {
        Timber.d("connectionInterrupted: ");
        connectionManager.release();
        BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.CONNECTION_INTERRUPTED));
    }

    @Override
    public void connectionError() {
        BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.CONNECTION_ERROR));
    }

    @Override
    public void connectionTimeout() {

    }

    @Override
    public void onMessageReceived(String message) {
        BusProvider.getInstance().post(new MessageEvent(message));
    }

    public class LocalBinder extends Binder {
        public ConnectionService getService(){
            return ConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        pingManager = new PingManager(host, PING_PORT);
        pingManager.setPingCallback(this);
    }

    public void connect() {
        if (!pingManager.isConnected()){
            BusProvider.getInstance().post(new ConnectionStatusEvent(ConnectionStatusEvent.START_CONNECTING));
            pingManager.init();
        }
    }

}

package pl.dp.rasbot;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Map;

import pl.dp.rasbot.connection.ConnectionManager;

/**
 * Created by Project4You S.C. on 29.08.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class ConnectionService extends Service{

    private final IBinder mbinder = new LocalBinder();

    private ConnectionManager connectionManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    public boolean isConnected() {
        return connectionManager.isConnected();
    }

    public void sendMessage(Map<String, String> data) {
        connectionManager.sendMessage(data);
    }

    public class LocalBinder extends Binder {
        public ConnectionService getService(){
            return ConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connectionManager = ConnectionManager.getInstance();
    }

    public void connect() throws IOException {
        if (!isConnected())
            connectionManager.connect();
    }

    public void setHandler(Handler handler){
        connectionManager.setHandler(handler);
    }
}

package pl.dp.rasbot.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import pl.dp.rasbot.ConnectionService;
import pl.dp.rasbot.R;
import pl.dp.rasbot.connection.PingCallback;
import pl.dp.rasbot.connection.wifi.WifiConnectionListener;
import pl.dp.rasbot.utils.BusProvider;
import timber.log.Timber;

/**
 * Created by dpodolak on 18.08.16.
 */
public class RobotActivity extends AppCompatActivity implements WifiConnectionListener, PingCallback{

    public ConnectionService connectionService;

    private boolean connectionServiceBound = false;

    private View parentLayout;

    private boolean isConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parentLayout = findViewById(android.R.id.content);

        startService();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionServiceBound) {
            unbindService(serviceConnection);
            connectionServiceBound = false;
        }
        BusProvider.getInstance().unregister(this);
    }

    public void startService() {
        Intent connectionIntent = new Intent(this, ConnectionService.class);
        bindService(connectionIntent, serviceConnection, BIND_AUTO_CREATE);
        startService(connectionIntent);

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            connectionService = ((ConnectionService.LocalBinder) iBinder).getService();
            connectionServiceBound = true;
            connectionService.addPingCallback(RobotActivity.this);
            connectionService.addWifiConnectionListener(RobotActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            connectionServiceBound = false;
            connectionService.removePingCallback(RobotActivity.this);
            connectionService.removeWifiConnectionListener(RobotActivity.this);
        }
    };

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void connectionEstablished() {
        Timber.d("RobotActivity:connectionEstablished: ");
        Snackbar.make(parentLayout, R.string.connected, Snackbar.LENGTH_LONG).show();
        isConnected = true;
    }

    @Override
    public void connectionInterrupted() {
        Timber.d("RobotActivity:connectionInterrupted: ");
        Snackbar.make(parentLayout, R.string.connection_interrupted, Snackbar.LENGTH_LONG).show();
        isConnected = false;

    }

    @Override
    public void connectionError() {
        Timber.d("RobotActivity:connectionError: ");
        Snackbar.make(parentLayout, R.string.connection_error, Snackbar.LENGTH_LONG).show();
        isConnected = false;
    }

    @Override
    public void connectionTimeout() {
        Timber.d("RobotActivity:connectionTimeout: ");
        Snackbar.make(parentLayout, R.string.connection_timeout, 1000).show();
        isConnected = false;
    }

    @Override
    public void wifiConnected() {
        Timber.d("RobotActivity:wifiConnected: ");
    }

    @Override
    public void networkFound() {
        Timber.d("RobotActivity:networkFound: ");
    }

    @Override
    public void networkNotFound() {
        Timber.d("RobotActivity:networkNotFound: ");
        Snackbar.make(parentLayout, R.string.wifi_not_found, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void wifiDisconnected() {
        Timber.d("RobotActivity:wifiDisconnected: ");
        Snackbar.make(parentLayout, R.string.wifi_disconnected, Snackbar.LENGTH_LONG).show();
    }
}

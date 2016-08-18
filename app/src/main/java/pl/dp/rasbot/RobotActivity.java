package pl.dp.rasbot;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import pl.dp.rasbot.utils.BusProvider;

/**
 * Created by dpodolak on 18.08.16.
 */
public class RobotActivity extends AppCompatActivity {

    public ConnectionService connectionService;

    private boolean connectionServiceBound = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            connectionServiceBound = false;
        }
    };
}

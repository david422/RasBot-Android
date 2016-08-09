package pl.dp.rasbot;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.dp.rasbot.event.ConnectionStatusEvent;
import pl.dp.rasbot.utils.BusProvider;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.rlMainActivityContainer)
    RelativeLayout containerRelativeLayout;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @BindView(R.id.tvMainActivityStatus)
    TextView mStatusTextView;

    @BindView(R.id.bMianActivitySterring)
    Button mSterringButton;

    @BindView(R.id.bMainActivityConnectToServer)
    Button mConnectingButton;

    @BindView(R.id.bMainActivityAbout)
    Button mAboutButton;

    @BindView(R.id.tvMainActivityConnectionStatus)
    TextView mConnectionStatus;

    @BindView(R.id.sConnectionProgressBar)
    ProgressBar progressBarCircularIndeterminate;

    private Snackbar snackbar;


    private ConnectionService connectionService;
    private boolean connectionServiceBound = false;

    /**
     * Show progress of searching network
     */
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);


        dialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.please_wait))
                .content(getString(R.string.search_for_rasbot_network))
                .progress(true, 0)
                .build();


        startService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionServiceBound) {
            unbindService(serviceConnection);
            connectionServiceBound = false;
        }

        compositeSubscription.clear();
    }

    @Subscribe
    public void connectionStatus(ConnectionStatusEvent event) {
        switch (event.getStatus()) {
            case ConnectionStatusEvent.START_CONNECTING:
                setDialogContent(R.string.connecting);
                dialog.show();
                break;
            case ConnectionStatusEvent.CONNECTION_ESTABLISHED:
                compositeSubscription.add(Observable.timer(2, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(i -> {
                            dialog.dismiss();
                            mSterringButton.setEnabled(true);
                        }));
                break;
            case ConnectionStatusEvent.CONNECTION_TIMEOUT:
                Snackbar.make(containerRelativeLayout, R.string.connection_timeout, 1000).show();
                break;
            case ConnectionStatusEvent.CONNECTION_INTERRUPTED:
                snackbar = Snackbar.make(containerRelativeLayout, R.string.connection_interrupted, Snackbar.LENGTH_LONG);
                snackbar.show();
                break;
            case ConnectionStatusEvent.CONNECTION_ERROR:
                compositeSubscription.add(Observable.timer(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(i -> {
                            dialog.dismiss();
                            snackbar = Snackbar.make(containerRelativeLayout, R.string.connection_error, Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }));
                break;
        }
    }


    private void setDialogContent(int content) {
        ((MaterialDialog) dialog).setContent(content);
    }

    @OnClick(R.id.bMainActivityConnectToServer)
    public void connect() {
        connectionService.connect();
    }


    @OnClick(R.id.bMianActivitySterring)
    public void sterring() {
        startActivity(new Intent(this, SterringActivity.class));
    }

    @OnClick(R.id.bMainActivityAbout)
    public void about() {
        new MaterialDialog.Builder(this)
                .title(R.string.about_title)
                .customView(R.layout.about_dialog, true)
                .positiveText(R.string.close)
                .show();

    }

    @Override
    protected void onStop() {
        super.onStop();

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

package pl.dp.rasbot;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.dp.rasbot.customview.WaitDialog;
import pl.dp.rasbot.utils.RasbotWifiManager;
import timber.log.Timber;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.tvMainActivityStatus)
    TextView mStatusTextView;

    @InjectView(R.id.bMianActivitySterring)
    ButtonRectangle mSterringButton;

    @InjectView(R.id.bMainActivityConnectToServer)
    ButtonRectangle mConnectingButton;

    @InjectView(R.id.bMainActivityAbout)
    ButtonRectangle mAboutButton;

    @InjectView(R.id.tvMainActivityConnectionStatus)
    TextView mConnectionStatus;

    @InjectView(R.id.sConnectionProgressBar)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminate;



    private Bus bus;

    public static final int ACTION_RASBOT_DISCOVERED = 0;
    public static final int ACTION_NETWORK_CONNECTED = 1;
    public static final int ACTION_NETWORK_DISCONNECTED = 3;
    public static final int ACTION_APPLICATION_CONNECTED = 2;

    private boolean isRasbotAppConnection = false;

    private ConnectionService connectionService;
    private boolean connectionServiceBound = false;

    /**
     * Show progress of searching network
     */
    private Dialog dialog;

    private RasbotWifiManager rasbotWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

//        rasbotWifiManager = new RasbotWifiManager(this);
//        rasbotWifiManager.setHandler(handler);

        try {
            dialog = new MaterialDialog.Builder(this)
                    .title(getString(R.string.please_wait))
                    .content(getString(R.string.search_for_rasbot_network))
                    .progress(true, 0)
                    .build();
        }catch (Exception e){
            dialog = new WaitDialog(this, getString(R.string.please_wait), getString(R.string.search_for_rasbot_network));
        }
//        mConnectingButton.setRippleSpeed(250);
//        mSterringButton.setRippleSpeed(250);
//        mSterringButton.setEnabled(false);
//        mAboutButton.setRippleSpeed(250);

        mAboutButton = (ButtonRectangle) findViewById(R.id.bMainActivityAbout);
        mConnectingButton = (ButtonRectangle) findViewById(R.id.bMainActivityConnectToServer);
        mConnectionStatus = (TextView) findViewById(R.id.tvMainActivityStatus);
        mSterringButton = (ButtonRectangle) findViewById(R.id.bMianActivitySterring);

        mAboutButton.setOnClickListener(v -> about());
        mSterringButton.setOnClickListener(v -> sterring());
        mConnectingButton.setOnClickListener( v -> connect());

        progressBarCircularIndeterminate = (ProgressBarCircularIndeterminate) findViewById(R.id.sConnectionProgressBar);


        startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, ConnectionService.class));
    }

    /* Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int status = msg.what;

            switch (status){
                case ACTION_RASBOT_DISCOVERED:
                    setDialogContent(R.string.rasbot_network_has_been_found);
                    break;
                case ACTION_NETWORK_CONNECTED:

                    setDialogContent(R.string.connect_with_rasbot_network);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connectionService.connect();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }, 1000);

                    break;

                case ACTION_APPLICATION_CONNECTED:
                    setDialogContent(R.string.connect_with_rasbot_application);
                    mSterringButton.setEnabled(true);
                    progressBarCircularIndeterminate.setVisibility(View.GONE);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 3000);

                    isRasbotAppConnection = true;
                    mSterringButton.setEnabled(true);
                    break;
                case ACTION_NETWORK_DISCONNECTED:
                    mConnectionStatus.setText("Szukanie sieci...");
                    isRasbotAppConnection = false;
                    mSterringButton.setEnabled(false);
                    progressBarCircularIndeterminate.setVisibility(View.VISIBLE);
                    break;

            }
        }
    };*/

    private void setDialogContent(int content){
        if (dialog instanceof MaterialDialog){
            ((MaterialDialog) dialog).setContent(content);
        }else{
            ((WaitDialog) dialog).setContent(content);
        }
    }

    @Override
    protected void onResume() {
        /*if (!isRasbotAppConnection) {
            rasbotWifiManager.onResume();
            if (!rasbotWifiManager.isRasbotConnection()) {
                rasbotWifiManager.searchForRasbotNetwork();
            }


            dialog.show();
        }*/
        super.onResume();
    }

    @Override
    protected void onPause() {
        /*if (rasbotWifiManager != null && !isRasbotAppConnection) {
            rasbotWifiManager.onPause();
            rasbotWifiManager.stopScanning();
        }*/
        super.onPause();
    }


    @OnClick(R.id.bMainActivityConnectToServer)
    public void connect(){

        connectionService.connect();


        /*if (!isRasbotAppConnection){
            rasbotWifiManager.searchForRasbotNetwork();
            dialog.show();
        }else{
            Toast.makeText(this, R.string.application_is_already_connectes, Toast.LENGTH_LONG).show();
        }*/
    }



    @OnClick(R.id.bMianActivitySterring)
    public void sterring(){
        startActivity(new Intent(this, SterringActivity.class));
    }

    @OnClick(R.id.bMainActivityAbout)
    public void about(){
        new MaterialDialog.Builder(this)
                .title(R.string.about_title)
                .customView(R.layout.about_dialog, true)
                .positiveText(R.string.close)
                .show();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (connectionServiceBound){
            unbindService(serviceConnection);
            connectionServiceBound = false;
        }
    }

    public void startService(){
        Intent connectionIntent = new Intent(this, ConnectionService.class);
        bindService(connectionIntent, serviceConnection, BIND_AUTO_CREATE);
        startService(connectionIntent);

    };

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

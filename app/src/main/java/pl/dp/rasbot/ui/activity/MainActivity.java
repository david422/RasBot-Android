package pl.dp.rasbot.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.dp.rasbot.R;
import pl.dp.rasbot.ui.fragment.SettingsFragment;
import pl.dp.rasbot.event.ConnectionStatusEvent;
import pl.dp.rasbot.event.MessageEvent;
import pl.dp.rasbot.message.camera.Camera1Message;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;


public class MainActivity extends RobotActivity {

    @BindView(R.id.rlMainActivityContainer)
    public RelativeLayout containerRelativeLayout;
    @BindView(R.id.tvMainActivityWifiStatus)
    public  TextView wifiStatusTextView;
    @BindView(R.id.tvMainActivityRobotStatus)
    public TextView robotStatusTextView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @BindView(R.id.tvMainActivityStatus)
    public TextView mStatusTextView;

    @BindView(R.id.bMianActivitySterring)
    public Button mSterringButton;

    @BindView(R.id.bMainActivityConnectToServer)
    public Button mConnectingButton;

    @BindView(R.id.bMainActivityAbout)
    public Button mAboutButton;

    @BindView(R.id.tvMainActivityConnectionStatus)
    public TextView mConnectionStatus;

    @BindView(R.id.sConnectionProgressBar)
    public ProgressBar progressBarCircularIndeterminate;




    /**
     * Show progress of searching network
     */
    private Dialog dialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
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
                .cancelable(false)
                .build();

        setRobotStatusTextView(getString(R.string.not_connected), Color.RED);
        setWifiStatusTextView(getString(R.string.not_connected), Color.RED);
        startService();
    }

    private void setRobotStatusTextView(String text, int color){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            robotStatusTextView.setText(Html.fromHtml(getString(R.string.status_robot_connection, color, text), Html.FROM_HTML_MODE_LEGACY));
        }else{
            robotStatusTextView.setText(Html.fromHtml(getString(R.string.status_robot_connection, color, text)));

        }
    }

    private void setWifiStatusTextView(String text, int color){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            wifiStatusTextView.setText(Html.fromHtml(getString(R.string.status_wifi_connection, color, text), Html.FROM_HTML_MODE_LEGACY));
        }else{
            String textToDisplay = getString(R.string.status_wifi_connection, color, text);
            Timber.d("MainActivity:setWifiStatusTextView: text: " +  textToDisplay);

            wifiStatusTextView.setText(Html.fromHtml(textToDisplay));
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    @Subscribe
    public void connectionStatus(ConnectionStatusEvent event) {
        switch (event.getStatus()) {
            case ConnectionStatusEvent.RASBOT_WIFI_NETWORK_SEARCHING:
                setDialogContent(R.string.wifi_searching);
                setWifiStatusTextView(getString(R.string.searching), Color.RED);
                dialog.show();
                break;
            case ConnectionStatusEvent.RASBOT_WIFI_NETWORK_FOUND:
                setDialogContent(R.string.wifi_found);
                setWifiStatusTextView(getString(R.string.found), Color.GREEN);
                break;
            case ConnectionStatusEvent.RASBOT_WIFI_NETWORK_NOT_FOUND:
                dialog.dismiss();
                setWifiStatusTextView(getString(R.string.not_connected), Color.RED);
                break;
            case ConnectionStatusEvent.RASBOT_WIFI_NETWORK_CONNECTED:
                setDialogContent(R.string.wifi_connected);
                setWifiStatusTextView(getString(R.string.connected), Color.GREEN);
                break;
            case ConnectionStatusEvent.RASBOT_WIFI_NETWORK_DISCONNECTED:
                dialog.dismiss();
                break;
            case ConnectionStatusEvent.START_CONNECTING:
                setDialogContent(R.string.connecting);
                if (!dialog.isShowing()){
                    dialog.show();
                }
                setRobotStatusTextView(getString(R.string.connecting), Color.RED);
                break;
            case ConnectionStatusEvent.CONNECTION_ESTABLISHED:
                setRobotStatusTextView(getString(R.string.connected), Color.GREEN);
                compositeSubscription.add(Observable.timer(2, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(i -> {
                            dialog.dismiss();
                            mSterringButton.setEnabled(true);
                        }));
                break;
            case ConnectionStatusEvent.CONNECTION_TIMEOUT:
                setRobotStatusTextView(getString(R.string.not_connected), Color.RED);
                break;
            case ConnectionStatusEvent.CONNECTION_INTERRUPTED:
                setRobotStatusTextView(getString(R.string.not_connected), Color.RED);
                break;
            case ConnectionStatusEvent.CONNECTION_ERROR:
                setRobotStatusTextView(getString(R.string.not_connected), Color.RED);
                compositeSubscription.add(Observable.timer(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(i -> {
                            dialog.dismiss();
                        }));
                break;
        }
    }

    @Subscribe
    public void onReceiveMessage(MessageEvent messageEvent) {
        Camera1Message c1m = new Gson().fromJson((String) messageEvent.getMessage().getObject(), Camera1Message.class);
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(this).edit();

        pref.putString(SettingsFragment.PREF_KEY_CAMERA_RESOLUTION, c1m.getResolution());
        pref.putInt(SettingsFragment.PREF_KEY_CAMERA_FPS, c1m.getFps());
        pref.putBoolean(SettingsFragment.PREF_KEY_CAMERA_FLIP_VERTICAL, c1m.isFlipVertical());
        pref.putBoolean(SettingsFragment.PREF_KEY_CAMERA_FLIP_HORIZONTAL, c1m.isFlipHorizontal());
        pref.putInt(SettingsFragment.PREF_KEY_CAMERA_BRIGHTNESS, c1m.getBrightness());

        pref.apply();

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
        if (isConnected()){
            startActivity(new Intent(this, SterringActivity.class));
        }
    }

    @OnClick(R.id.bMainActivityAbout)
    public void about() {

        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogView  = layoutInflater.inflate(R.layout.about_dialog, null);
        TextView version = (TextView) dialogView.findViewById(R.id.tvAboutDialogVersion);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(String.format(" v%s", packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new MaterialDialog.Builder(this)
                .title(R.string.about_title)
                .customView(dialogView, true)
                .positiveText(R.string.close)
                .show();

    }


}

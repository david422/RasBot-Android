package pl.dp.rasbot;

import android.animation.Animator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import pl.dp.rasbot.customview.Slider;
import pl.dp.rasbot.message.LeftControl;
import pl.dp.rasbot.message.RightControl;
import pl.dp.rasbot.streaming.StreamingManager;
import pl.dp.rasbot.utils.AnimatorHelper;

/**
 * Created by Project4You S.C. on 02.05.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class SterringActivity extends FragmentActivity implements SurfaceHolder.Callback{

    @BindView(R.id.sSterringActivityLeftSlider)
    Slider mLeftSlider;
    @BindView(R.id.sSterringActivityRightSlider)
    Slider mRightSlider;
    @BindView(R.id.tvSterringActivityLeftValue)
    TextView mLeftValueTextView;
    @BindView(R.id.tvSterringActivityRightValue)
    TextView mRightValueTextView;

    @BindView(R.id.surfView)
    SurfaceView mSurfaceView;

    @BindView(R.id.rlSteeringActivityCameraView)
    RelativeLayout cameraViewRelativeLayout;

    private StreamingManager streamingManager;

    private ConnectionService connectionService;
    private boolean connectionServiceBound = false;

    private boolean settingEnabled;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sterring_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);

        streamingManager = new StreamingManager(this);

        mLeftSlider.setOnSliderValueChanged(onLeftSliderChanged);
        mRightSlider.setOnSliderValueChanged(onRightSliderChanged);

        SurfaceHolder sh = mSurfaceView.getHolder();
        sh.addCallback(this);

        startService();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionServiceBound){
            unbindService(serviceConnection);
            connectionServiceBound = false;
        }
    }

    Slider.OnSliderValueChanged onLeftSliderChanged = new Slider.OnSliderValueChanged() {
        @Override
        public void onSliderValueChanged(int value) {
            mLeftValueTextView.setText(value + " rpm");
            if (connectionService.isConnected()){
                connectionService.sendMessage(new LeftControl(value));
            }
        }
    };

    Slider.OnSliderValueChanged onRightSliderChanged = new Slider.OnSliderValueChanged() {
        @Override
        public void onSliderValueChanged(int value) {
            mRightValueTextView.setText(value + " rpm");
            if (connectionService.isConnected())
                connectionService.sendMessage(new RightControl(value));
        }
    };



    @OnClick(R.id.bSterringActivityPlay)
    public void onPlaylick(Button button){

        if (!streamingManager.isPlaying()) {
            streamingManager.play();
            button.setText("Zatrzymaj kamerę");
        } else {
            streamingManager.pause();
            button.setText(getString(R.string.run_camera));
        }
    }

    @OnClick(R.id.bSterringActivitySettings)
    public void settings(){

        int sliderXOffset;
        int cameraViewOffset;


        if (settingEnabled){
            sliderXOffset = 0;
            cameraViewOffset = 0;
            settingEnabled = false;
        }else{
            sliderXOffset = mLeftSlider.getWidth();
            cameraViewOffset = -sliderXOffset;
            settingEnabled = true;
        }

        if (settingEnabled) {
            mLeftSlider.animate().translationX(-sliderXOffset).start();
            mRightSlider.animate().translationX(sliderXOffset).setListener(new AnimatorHelper() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    cameraViewRelativeLayout.animate().translationX(cameraViewOffset).start();
                    mRightSlider.animate().setListener(null);
                    enterFragment();
                }
            }).start();
        }else{
            cameraViewRelativeLayout.animate().translationX(cameraViewOffset).setListener(new AnimatorHelper(){
                @Override
                public void onAnimationEnd(Animator animator) {
                    mLeftSlider.animate().translationX(-sliderXOffset).start();
                    mRightSlider.animate().translationX(sliderXOffset).start();
                    cameraViewRelativeLayout.animate().setListener(null);

                    getSupportFragmentManager().beginTransaction().remove(settingsFragment);
                }
            }).start();
        }
    }

    public void enterFragment(){
        FragmentTransaction fragmentManager =  getSupportFragmentManager().beginTransaction();

        fragmentManager.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }

        fragmentManager.add(R.id.fragmentTest, settingsFragment, "fragmentTag");
        fragmentManager.commit();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        streamingManager.init();
        streamingManager.setSurfaceView(surfaceHolder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        streamingManager.release();
    }

    public void startService(){
        Intent connectionIntent = new Intent(this, ConnectionService.class);
        bindService(connectionIntent, serviceConnection, BIND_AUTO_CREATE);

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

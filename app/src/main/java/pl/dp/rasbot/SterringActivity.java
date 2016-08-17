package pl.dp.rasbot;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.support.v4.app.FragmentActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
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
import timber.log.Timber;

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

    @BindView(R.id.bSterringActivityPlay)
    Button runCameraButton;

    @BindView(R.id.bSterringActivitySettings)
    Button settingsButton;


    @BindView(R.id.surfView)
    SurfaceView mSurfaceView;

    @BindView(R.id.rlSteeringActivityCameraView)
    RelativeLayout cameraViewRelativeLayout;

    @BindView(R.id.fragmentTest)
    FrameLayout settingsFrameLayout;

    private StreamingManager streamingManager;

    private ConnectionService connectionService;
    private boolean connectionServiceBound = false;

    private boolean settingEnabled;
    private SettingsFragment settingsFragment;

    private RelativeLayout.LayoutParams originaParams;



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
        float scale;
        int settingTranslation;

        if (settingEnabled){
            sliderXOffset = 0;
            cameraViewOffset = 0;
            scale = 1;
            settingEnabled = false;
            settingTranslation = settingsFrameLayout.getWidth();
        }else{
            sliderXOffset = mLeftSlider.getWidth();
            scale = getScale();
            cameraViewOffset = (int) (-sliderXOffset - ((1-scale)/2 * cameraViewRelativeLayout.getWidth()));
            settingEnabled = true;
            settingTranslation = 0;
            settingsFrameLayout.setTranslationX(settingsFrameLayout.getWidth());
        }

        AnimatorSet sliderAnimatorSet = new AnimatorSet();
        AnimatorSet camerViewAnimatorSet = new AnimatorSet();
        AnimatorSet mainAnimatorSet = new AnimatorSet();


        ObjectAnimator leftSliderObjectAnimator = ObjectAnimator.ofFloat(mLeftSlider, View.TRANSLATION_X, -sliderXOffset);
        ObjectAnimator rightSliderObjectAnimator = ObjectAnimator.ofFloat(mRightSlider, View.TRANSLATION_X, sliderXOffset);
        sliderAnimatorSet.playTogether(leftSliderObjectAnimator, rightSliderObjectAnimator);


        ValueAnimator scaleCamerView = ObjectAnimator.ofFloat(cameraViewRelativeLayout, View.SCALE_X, scale);
        ObjectAnimator translaterCamerView = ObjectAnimator.ofFloat(cameraViewRelativeLayout, View.TRANSLATION_X, cameraViewOffset);
        ObjectAnimator fragmentTranslation = ObjectAnimator.ofFloat(settingsFrameLayout, View.TRANSLATION_X, settingTranslation);
        camerViewAnimatorSet.playTogether(scaleCamerView, translaterCamerView, fragmentTranslation);


        scaleCamerView.addUpdateListener(valueAnimator -> {
            float buttonScale = 1/cameraViewRelativeLayout.getScaleX();
            runCameraButton.setTextScaleX(buttonScale);
            settingsButton.setTextScaleX(buttonScale);
        });

        if (settingEnabled) {


            camerViewAnimatorSet.addListener(new AnimatorHelper(){
                @Override
                public void onAnimationStart(Animator animator) {
                    enterFragment();
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    streamingManager.refresh();
                }
            });

            mainAnimatorSet.playSequentially(sliderAnimatorSet, camerViewAnimatorSet);
            mainAnimatorSet.start();
        }else{

            mSurfaceView.setLayoutParams(originaParams);

            camerViewAnimatorSet.addListener(new AnimatorHelper(){
                @Override
                public void onAnimationEnd(Animator animator) {


                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.remove(settingsFragment).commit();
                }
            });


            mainAnimatorSet.playSequentially(camerViewAnimatorSet, sliderAnimatorSet);
            mainAnimatorSet.start();
        }
    }

    public void enterFragment(){
        FragmentTransaction fragmentTransaction =  getFragmentManager().beginTransaction();

        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
            settingsFragment.setConnectionService(connectionService);
            settingsFragment.setStreamingManager(streamingManager);
        }

        fragmentTransaction.add(R.id.fragmentTest, settingsFragment, "fragmentTag");
        fragmentTransaction.commit();
    }

    private float getScale(){
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cameraViewWidth = cameraViewRelativeLayout.getWidth();
        int settingsViewWidth = settingsFrameLayout.getWidth();

        int displayDiff = cameraViewWidth + settingsViewWidth - screenWidth;

        if (displayDiff <= 0){
            return 0f;
        }

        float scale = (float)(cameraViewWidth - displayDiff)/cameraViewWidth;
        Timber.d("getScale: scale %f", scale);
        return scale;

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        originaParams = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
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

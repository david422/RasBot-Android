package pl.dp.rasbot.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.os.Bundle;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import pl.dp.rasbot.R;
import pl.dp.rasbot.customview.Slider;
import pl.dp.rasbot.event.ConnectionStatusEvent;
import pl.dp.rasbot.message.LeftControl;
import pl.dp.rasbot.message.RightControl;
import pl.dp.rasbot.streaming.StreamingManager;
import pl.dp.rasbot.ui.fragment.SettingsFragment;
import pl.dp.rasbot.utils.AnimatorHelper;
import timber.log.Timber;

/**
 * Created by Project4You S.C. on 02.05.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class SterringActivity extends RobotActivity implements SurfaceHolder.Callback{

    @BindView(R.id.sSterringActivityLeftSlider)
    public Slider mLeftSlider;
    @BindView(R.id.sSterringActivityRightSlider)
    public Slider mRightSlider;
    @BindView(R.id.tvSterringActivityLeftValue)
    public TextView mLeftValueTextView;
    @BindView(R.id.tvSterringActivityRightValue)
    public TextView mRightValueTextView;

    @BindView(R.id.bSterringActivityPlay)
    public Button runCameraButton;

    @BindView(R.id.bSterringActivitySettings)
    public Button settingsButton;


    @BindView(R.id.surfView)
    public SurfaceView mSurfaceView;

    @BindView(R.id.rlSteeringActivityCameraView)
    public RelativeLayout cameraViewRelativeLayout;

    @BindView(R.id.fragmentTest)
    public FrameLayout settingsFrameLayout;

    private StreamingManager streamingManager;

    private boolean settingEnabled;
    private SettingsFragment settingsFragment;

    private RelativeLayout.LayoutParams originaParams;

    private Dialog connectionInterruptedDialog;
    private Dialog connectingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sterring_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);


        streamingManager = new StreamingManager(this);

        mLeftSlider.setOnSliderValueChanged(onLeftSliderChanged);
        mRightSlider.setOnSliderValueChanged(onRightSliderChanged);


        SurfaceHolder sh = mSurfaceView.getHolder();
        sh.addCallback(this);
    }

    @Subscribe
    public void connectionStatus(ConnectionStatusEvent event){
        switch (event.getStatus()){
            case ConnectionStatusEvent.RASBOT_WIFI_NETWORK_NOT_FOUND:
            case ConnectionStatusEvent.RASBOT_WIFI_NETWORK_DISCONNECTED:
            case ConnectionStatusEvent.CONNECTION_ERROR:
            case ConnectionStatusEvent.CONNECTION_TIMEOUT:
            case ConnectionStatusEvent.CONNECTION_INTERRUPTED:
                connectionInterruptedDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.connection_with_robot_has_been_interrupted)
                        .setMessage(R.string.choose_action)
                        .setCancelable(false)
                        .setPositiveButton(R.string.connect_again, (dialogInterface, i) -> {
                            connectionService.connect();
                            dialogInterface.dismiss();
                            buildWaitDialog();
                        })
                        .setNegativeButton(R.string.exit, (dialogInterface1, i1) -> finish())
                        .create();
                connectionInterruptedDialog.show();
                break;
            case ConnectionStatusEvent.CONNECTION_ESTABLISHED:
                if (connectingDialog != null && connectingDialog.isShowing()){
                    connectingDialog.dismiss();
                }

                streamingManager.refresh();
                break;
        }
    }

    private void buildWaitDialog() {
        connectingDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.please_wait))
                .content(getString(R.string.connecting))
                .progress(true, 0)
                .cancelable(false)
                .build();
        connectingDialog.show();
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

        Timber.d("SterringActivity:settings: ");
        if (settingEnabled){

            Timber.d("SterringActivity:settings: fragment setting changed: " + settingsFragment.isSettingsChanged());
            if (settingsFragment.isSettingsChanged()){new AlertDialog.Builder(this)
                        .setTitle(R.string.settings_have_not_been_saved)
                        .setMessage(R.string.do_you_want_save_your_settings)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                            settingsFragment.saveSettings();
                            settingsFragment.setOnCloseListener(() -> settings());
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                            settingsFragment.resetSettings();
                            settings();
                            dialogInterface.dismiss();
                        }).show();
                return;
            }

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

}

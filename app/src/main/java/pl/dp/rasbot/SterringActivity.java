package pl.dp.rasbot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.gstreamer.GStreamer;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.dp.rasbot.customview.Slider;

/**
 * Created by Project4You S.C. on 02.05.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class SterringActivity extends Activity implements SurfaceHolder.Callback{

    @InjectView(R.id.sSterringActivityLeftSlider)
    Slider mLeftSlider;
    @InjectView(R.id.sSterringActivityRightSlider)
    Slider mRightSlider;
    @InjectView(R.id.tvSterringActivityLeftValue)
    TextView mLeftValueTextView;
    @InjectView(R.id.tvSterringActivityRightValue)
    TextView mRightValueTextView;



    @InjectView(R.id.surfView)
    SurfaceView mSurfaceView;

    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data


    private ConnectionService connectionService;
    private boolean connectionServiceBound = false;

    Map<String, String> data = new HashMap<>();

    private boolean isPlaying = false;   // Whether the user asked to go to PLAYING

    private Socket socket;

    private DataOutputStream outToServer;

    static {
        System.loadLibrary("gstreamer_android");

        System.loadLibrary("tutorial-3");
        nativeClassInit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sterring_activity);

        try {
            GStreamer.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.inject(this);


        mLeftSlider = (Slider) findViewById(R.id.sSterringActivityLeftSlider);
        mRightSlider = (Slider) findViewById(R.id.sSterringActivityRightSlider);
        mLeftValueTextView = (TextView) findViewById(R.id.tvSterringActivityLeftValue);
        mRightValueTextView = (TextView) findViewById(R.id.tvSterringActivityRightValue);

        findViewById(R.id.bSterringActivityPlay).setOnClickListener(v -> onPlaylick((Button) v));

        mSurfaceView = (SurfaceView) findViewById(R.id.surfView);

        mLeftSlider.setOnSliderValueChanged(onLeftSliderChanged);
        mRightSlider.setOnSliderValueChanged(onRightSliderChanged);



        data.put("right_rpm", "0");
        data.put("left_rpm", "0");

        SurfaceHolder sh = mSurfaceView.getHolder();
        sh.addCallback(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            nativeInit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isPlaying)
            nativePause();

        try{
            nativeFinalize();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    Slider.OnSliderValueChanged onLeftSliderChanged = new Slider.OnSliderValueChanged() {
        @Override
        public void onSliderValueChanged(int value) {
            mLeftValueTextView.setText(value + " rpm");
            data.put("left_rpm", String.valueOf(value));
            if (connectionService.isConnected())
                connectionService.sendMessage(data);
        }
    };

    Slider.OnSliderValueChanged onRightSliderChanged = new Slider.OnSliderValueChanged() {
        @Override
        public void onSliderValueChanged(int value) {
            mRightValueTextView.setText(value + " rpm");
            data.put("right_rpm", String.valueOf(value));
            if (connectionService.isConnected())
                connectionService.sendMessage(data);
        }
    };




    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i("GStreamer", "Gst initialized. Restoring state, playing:" + isPlaying);
        // Restore previous playing state
        if (isPlaying) {
            nativePlay();
        } else {
            nativePause();
        }

    }

    @OnClick(R.id.bSterringActivityPlay)
    public void onPlaylick(Button button){

        if (!isPlaying) {
            nativePlay();
            button.setText("Zatrzymaj kamerę");
            isPlaying = true;
        } else {
            nativePause();

            button.setText(getString(R.string.run_camera));
            isPlaying = false;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {
        nativeSurfaceInit (holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        nativeSurfaceFinalize ();
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService();
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

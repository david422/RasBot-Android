package pl.dp.rasbot;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.dp.rasbot.connection.ConnectionManager;
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

    @InjectView(R.id.vvStreamVideo)
    VideoView mStreamVideoView;

    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mediaPlayer;

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

    private String streamAddress = "http://192.168.42.136:8160";

    private ConnectionManager mConnectionManager;
    Map<String, String> data = new HashMap<>();

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING

    static {
//        System.loadLibrary("gstreamer_android");
//        System.loadLibrary("tutorial-3");
//        nativeClassInit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sterring_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.inject(this);

        mLeftSlider.setOnSliderValueChanged(onLeftSliderChanged);
        mRightSlider.setOnSliderValueChanged(onRightSliderChanged);

        mConnectionManager = ConnectionManager.getInstance();

        data.put("right_rpm", "0");
        data.put("left_rpm", "0");

    }

    @Override
    protected void onResume() {
        super.onResume();
//        mStreamVideoView.st/art();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mStreamVideoView.stopPlayback();
        if (mediaPlayer!=null)
        mediaPlayer.stop();
    }

    Slider.OnSliderValueChanged onLeftSliderChanged = new Slider.OnSliderValueChanged() {
        @Override
        public void onSliderValueChanged(int value) {
            mLeftValueTextView.setText(value + " rpm");
            data.put("left_rpm", String.valueOf(value));
            if (mConnectionManager.isConnected())
                mConnectionManager.sendMessage(data);
        }
    };

    Slider.OnSliderValueChanged onRightSliderChanged = new Slider.OnSliderValueChanged() {
        @Override
        public void onSliderValueChanged(int value) {
            mRightValueTextView.setText(value + " rpm");
            data.put("right_rpm", String.valueOf(value));
            if (mConnectionManager.isConnected())
                mConnectionManager.sendMessage(data);
        }
    };


    SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDisplay(mSurfaceHolder);
                mediaPlayer.setDataSource(streamAddress);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(onPreparedListener);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    };

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
        }
    };

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i("GStreamer", "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }

        // Re-enable buttons, now that GStreamer is initialized
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            public void run() {
//                activity.findViewById(R.id.button_play).setEnabled(true);
//                activity.findViewById(R.id.button_stop).setEnabled(true);
            }
        });
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
}

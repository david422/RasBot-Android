package pl.dp.rasbot.streaming;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.gstreamer.GStreamer;

/**
 * Created by dpodolak on 09.08.16.
 */
public class StreamingManager {


    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data


    private boolean isPlaying;

    private Context context;

    static {
        System.loadLibrary("gstreamer_android");

        System.loadLibrary("tutorial-3");
        nativeClassInit();
    }

    public StreamingManager(Context context) {
        this.context = context;

        try {
            GStreamer.init(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(){
        nativeInit();
    }

    public void play(){
        if (nativeClassInit()){
            nativePlay();
            isPlaying = true;
        }
    }

    public void pause(){
        if (isPlaying){
            nativePause();
            isPlaying = false;
        }
    }

    Surface surface;

    public void setSurfaceView(Surface surface){
        this.surface = surface;
        nativeSurfaceInit(surface);
    }

    public void refresh(){
        nativePause();
        nativeFinalize();
        nativeSurfaceFinalize();
        nativeInit();
        nativeSurfaceInit(surface);
        nativePlay();
    }



    public void release(){
        nativeFinalize();
        nativeSurfaceFinalize();
    }


    public boolean isPlaying() {
        return isPlaying;
    }

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

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
    }
}

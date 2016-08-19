package pl.dp.rasbot.ui.fragment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Subscribe;

import pl.dp.rasbot.ConnectionService;
import pl.dp.rasbot.R;
import pl.dp.rasbot.event.MessageEvent;
import pl.dp.rasbot.message.camera.Camera1Message;
import pl.dp.rasbot.preference.PreferencePicker;
import pl.dp.rasbot.streaming.StreamingManager;
import pl.dp.rasbot.utils.BusProvider;
import timber.log.Timber;

/**
 * Created by dpodolak on 09.08.16.
 */
public class SettingsFragment extends PreferenceFragment {

    public static final String PREF_KEY_CAMERA_FPS = "pref_key_camera_fps";
    public static final String PREF_KEY_CAMERA_RESOLUTION = "pref_key_camera_resolution";
    public static final String PREF_KEY_CAMERA_BRIGHTNESS = "pref_key_camera_brightness";
    public static final String PREF_KEY_CAMERA_FLIP_VERTICAL = "pref_key_camera_flip_vertical";
    public static final String PREF_KEY_CAMERA_FLIP_HORIZONTAL = "pref_key_camera_flip_horizontal";

    private ConnectionService connectionService;
    private StreamingManager streamingManager;

    private Preference resolutionPreference;
    private PreferencePicker fpsPreference;
    private PreferencePicker brighnessPreference;
    private Preference flipVertPreference;
    private Preference flipHorizontalPreference;

    private Preference preferenceButton;
    private Camera1Message camera1Message;

    private Dialog waitDialog;
    private SharedPreferences prefs;

    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public void setStreamingManager(StreamingManager streamingManager) {
        this.streamingManager = streamingManager;
    }

    private boolean closeOnSaved;

    private boolean settingsChanged;

    private Camera1Message defaultSettings;

    private SaveOnCloseListener onCloseListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        resolutionPreference = findPreference(PREF_KEY_CAMERA_RESOLUTION);
        fpsPreference = (PreferencePicker) findPreference(PREF_KEY_CAMERA_FPS);
        brighnessPreference = (PreferencePicker) findPreference(PREF_KEY_CAMERA_BRIGHTNESS);
        flipVertPreference = findPreference(PREF_KEY_CAMERA_FLIP_VERTICAL);
        flipHorizontalPreference = findPreference(PREF_KEY_CAMERA_FLIP_HORIZONTAL);
        preferenceButton = findPreference("pref_key_save_button");

        resolutionPreference.setOnPreferenceChangeListener(this::settingsPreferenceChanged);
        fpsPreference.setOnPreferenceChangeListener(this::settingsPreferenceChanged);
        brighnessPreference.setOnPreferenceChangeListener(this::settingsPreferenceChanged);
        flipVertPreference.setOnPreferenceChangeListener(this::settingsPreferenceChanged);
        flipHorizontalPreference.setOnPreferenceChangeListener(this::settingsPreferenceChanged);

        preferenceButton.setOnPreferenceClickListener(this::onPreferenceButton);

        BusProvider.getInstance().register(this);
        camera1Message = new Camera1Message();

        waitDialog = new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.please_wait))
                .progress(true, 0)
                .cancelable(false)
                .build();
        prepareDefaultSettings();

    }

    public void setOnCloseListener(SaveOnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    private void prepareDefaultSettings(){
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        defaultSettings = new Camera1Message();

        defaultSettings.setResolution(prefs.getString(PREF_KEY_CAMERA_RESOLUTION, getResources().getStringArray(R.array.camera_resolution_values)[0]));
        defaultSettings.setFps(prefs.getInt(PREF_KEY_CAMERA_FPS, 25));
        defaultSettings.setBrightness(prefs.getInt(PREF_KEY_CAMERA_BRIGHTNESS, 60));
        defaultSettings.setFlipHorizontal(prefs.getBoolean(PREF_KEY_CAMERA_FLIP_HORIZONTAL, true));
        defaultSettings.setFlipVertical(prefs.getBoolean(PREF_KEY_CAMERA_FLIP_VERTICAL, true));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onMessageReceived(MessageEvent event) {
        Timber.d("onMessageReceived: %s", event.getMessage());

        new Handler().postDelayed(() -> {
            streamingManager.refresh();
            waitDialog.dismiss();
            settingsChanged = false;

            if (closeOnSaved && onCloseListener != null){
                closeOnSaved = false;
                onCloseListener.onClose();

            }
        }, 2000);
    }

    public boolean onPreferenceButton(Preference preference) {
        if (connectionService != null && streamingManager != null){
            connectionService.sendMessage(camera1Message);
            waitDialog.show();
        }
        return true;
    }

    public boolean isSettingsChanged() {
        return settingsChanged;
    }

    private boolean settingsPreferenceChanged(Preference preference, Object newValue){

        settingsChanged = true;
        switch (preference.getKey()){
            case PREF_KEY_CAMERA_RESOLUTION:
                camera1Message.setResolution((String) newValue);
                break;
            case PREF_KEY_CAMERA_FPS:
                camera1Message.setFps((Integer) newValue);
                break;
            case PREF_KEY_CAMERA_BRIGHTNESS:
                camera1Message.setBrightness((Integer) newValue);
                break;
            case PREF_KEY_CAMERA_FLIP_HORIZONTAL:
                camera1Message.setFlipHorizontal((Boolean) newValue);
                break;
            case PREF_KEY_CAMERA_FLIP_VERTICAL:
                camera1Message.setFlipVertical((Boolean) newValue);
                break;
        }

        return true;
    }

    public void saveSettings() {
        closeOnSaved = true;
        if (connectionService != null && streamingManager != null){
            connectionService.sendMessage(camera1Message);
            waitDialog.show();
        }

    }

    public void resetSettings() {
        settingsChanged = false;


        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREF_KEY_CAMERA_RESOLUTION, defaultSettings.getResolution());
        editor.putInt(PREF_KEY_CAMERA_FPS, defaultSettings.getFps());
        editor.putInt(PREF_KEY_CAMERA_BRIGHTNESS, defaultSettings.getBrightness());
        editor.putBoolean(PREF_KEY_CAMERA_FLIP_VERTICAL, defaultSettings.isFlipVertical());
        editor.putBoolean(PREF_KEY_CAMERA_FLIP_HORIZONTAL, defaultSettings.isFlipHorizontal());

        editor.apply();
    }

    public interface SaveOnCloseListener{
        void onClose();
    }
}
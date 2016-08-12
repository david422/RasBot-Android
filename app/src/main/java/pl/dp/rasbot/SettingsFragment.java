package pl.dp.rasbot;

import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.squareup.otto.Subscribe;

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

    private ConnectionService connectionService;
    private StreamingManager streamingManager;

    private Preference resolutionPreference;
    private PreferencePicker fpsPreference;
    private PreferencePicker brighnessPreference;
    private Preference flipVertPreference;
    private Preference flipHorizontalPreference;

    private Preference preferenceButton;
    private Camera1Message camera1Message;

    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public void setStreamingManager(StreamingManager streamingManager) {
        this.streamingManager = streamingManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        resolutionPreference = findPreference("pref_key_camera_resolution");
        fpsPreference = (PreferencePicker) findPreference("pref_key_camera_fps");
        brighnessPreference = (PreferencePicker) findPreference("pref_key_camera_brightness");
        flipVertPreference = findPreference("pref_key_camera_flip_vertical");
        flipHorizontalPreference = findPreference("pref_key_camera_flip_horizontal");
        preferenceButton = findPreference("pref_key_save_button");

        resolutionPreference.setOnPreferenceChangeListener(this::resolutionPreferenceChanged);
        fpsPreference.setOnPreferenceChangeListener(this::fpsPreferenceChanged);
        brighnessPreference.setOnPreferenceChangeListener(this::brightnessPreferenceChanged);
        flipVertPreference.setOnPreferenceChangeListener(this::flipVerticalPreferenceChanged);
        flipHorizontalPreference.setOnPreferenceChangeListener(this::horizontalVerticalPreferenceChanged);

        preferenceButton.setOnPreferenceClickListener(this::onPreferenceButton);

        BusProvider.getInstance().register(this);
        camera1Message = new Camera1Message();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onMessageReceived(MessageEvent event) {
        Timber.d("onMessageReceived: %s", event.getMessage());

        new Handler().postDelayed(() -> streamingManager.refresh(), 2000);
    }

    public boolean onPreferenceButton(Preference preference) {
        if (connectionService != null && streamingManager != null){
            connectionService.sendMessage(camera1Message);
        }
        return true;
    }

    private boolean resolutionPreferenceChanged(Preference pref, Object newValue) {

        camera1Message.setResolution((String) newValue);
        Timber.d("resolutionPreferenceChanged: ");
        return true;
    }

    private boolean fpsPreferenceChanged(Preference preference, Object newValue) {
        camera1Message.setFps((Integer) newValue);
        return true;
    }

    private boolean brightnessPreferenceChanged(Preference preference, Object newValue) {
        Timber.d("brightnessPreferenceChanged: ");
        camera1Message.setBrightness((Integer) newValue);
        return true;
    }

    private boolean flipVerticalPreferenceChanged(Preference preference, Object newValue) {
        Timber.d("flipVerticalPreferenceChanged: ");
        camera1Message.setFlipVertical((Boolean) newValue);
        return true;
    }

    private boolean horizontalVerticalPreferenceChanged(Preference preference, Object newValue) {
        Timber.d("horizontalVerticalPreferenceChanged: ");
        camera1Message.setHorizontalVertical((Boolean) newValue);
        return true;
    }


}

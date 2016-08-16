package pl.dp.rasbot.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dpodolak on 09.08.16.
 */
public class ConnectionStatusEvent {


    public static final int RASBOT_WIFI_NETWORK_SEARCHING   = 0;
    public static final int RASBOT_WIFI_NETWORK_FOUND       = 1;
    public static final int RASBOT_WIFI_NETWORK_NOT_FOUND   = 2;
    public static final int RASBOT_WIFI_NETWORK_CONNECTED   = 3;
    public static final int RASBOT_WIFI_NETWORK_DISCONNECTED = 4;
    public static final int START_CONNECTING                = 5;
    public static final int CONNECTION_ESTABLISHED          = 6;
    public static final int CONNECTION_INTERRUPTED          = 7;
    public static final int CONNECTION_TIMEOUT              = 8;
    public static final int CONNECTION_ERROR                = 9;

    @IntDef({RASBOT_WIFI_NETWORK_SEARCHING, RASBOT_WIFI_NETWORK_NOT_FOUND, RASBOT_WIFI_NETWORK_FOUND,
            RASBOT_WIFI_NETWORK_CONNECTED, RASBOT_WIFI_NETWORK_DISCONNECTED, CONNECTION_ESTABLISHED, START_CONNECTING, CONNECTION_INTERRUPTED,
            CONNECTION_TIMEOUT, CONNECTION_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectionState{}

    private @ConnectionState int status;

    public ConnectionStatusEvent(@ConnectionState int status) {
        this.status = status;
    }

    public @ConnectionState int getStatus() {
        return status;
    }
}

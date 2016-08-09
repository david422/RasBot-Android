package pl.dp.rasbot.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dpodolak on 09.08.16.
 */
public class ConnectionStatusEvent {


    public static final int START_CONNECTING = 0;
    public static final int CONNECTION_ESTABLISHED = 1;
    public static final int CONNECTION_INTERRUPTED = 2;
    public static final int CONNECTION_TIMEOUT = 3;
    public static final int CONNECTION_ERROR = 4;

    @IntDef({CONNECTION_ESTABLISHED, START_CONNECTING, CONNECTION_INTERRUPTED, CONNECTION_TIMEOUT, CONNECTION_ERROR})
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
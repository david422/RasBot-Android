package pl.dp.rasbot.connection.wifi;

/**
 * Created by dpodolak on 16.08.16.
 */
public interface WifiConnectionListener {

    void connected();

    void networkFound();

    void networkNotFound();

    void disconnected();

}

package pl.dp.rasbot.connection;

/**
 * Created by dawidpodolak on 07.08.16.
 */
public interface PingCallback {

    public void connectionEstablished();

    public void connectionInterrupted();

    public void connectionTimeout();
}

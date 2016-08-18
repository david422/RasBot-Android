package pl.dp.rasbot.connection;


import pl.dp.rasbot.message.ReceivedMessage;

/**
 * Created by dawidpodolak on 08.08.16.
 */
public interface MessageCallback {

    void onMessageReceived(ReceivedMessage message);

}

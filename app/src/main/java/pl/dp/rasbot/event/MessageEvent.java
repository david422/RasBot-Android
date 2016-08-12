package pl.dp.rasbot.event;

import pl.dp.rasbot.message.ReceivedMessage;

public class MessageEvent {
    private ReceivedMessage message;

    public MessageEvent(ReceivedMessage message) {
        this.message = message;
    }

    public ReceivedMessage getMessage() {
        return message;
    }
}
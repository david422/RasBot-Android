package pl.dp.rasbot.message.camera;

import pl.dp.rasbot.message.Message;

/**
 * Created by dawidpodolak on 11.08.16.
 */
public abstract class CameraMessage extends Message {

    public CameraMessage(Object parameter) {
        super(parameter);
    }

    @Override
    public String getType() {
        return "camera";
    }
}

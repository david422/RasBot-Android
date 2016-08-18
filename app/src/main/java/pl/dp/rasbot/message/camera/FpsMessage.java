package pl.dp.rasbot.message.camera;


/**
 * Created by dawidpodolak on 11.08.16.
 */
public class FpsMessage extends CameraMessage {

    public FpsMessage(Object parameter) {
        super(parameter);
    }

    @Override
    public String getCommand() {
        return "fps";
    }
}

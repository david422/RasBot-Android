package pl.dp.rasbot.message.camera;


/**
 * Created by dawidpodolak on 11.08.16.
 */
public class FlipVerticalMessage extends CameraMessage {

    public FlipVerticalMessage(Object parameter) {
        super(parameter);
    }

    @Override
    public String getCommand() {
        return "fv";
    }
}

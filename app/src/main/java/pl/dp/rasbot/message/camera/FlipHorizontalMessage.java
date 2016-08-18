package pl.dp.rasbot.message.camera;


/**
 * Created by dawidpodolak on 11.08.16.
 */
public class FlipHorizontalMessage extends CameraMessage {

    public FlipHorizontalMessage(Object parameter) {
        super(parameter);
    }

    @Override
    public String getCommand() {
        return "fh";
    }
}

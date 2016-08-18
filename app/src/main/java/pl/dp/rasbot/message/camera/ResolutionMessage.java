package pl.dp.rasbot.message.camera;

/**
 * Created by dawidpodolak on 11.08.16.
 */
public class ResolutionMessage extends CameraMessage {

    public ResolutionMessage(Object parameter) {
        super(parameter);
    }

    @Override
    public String getCommand() {
        return "res";
    }
}

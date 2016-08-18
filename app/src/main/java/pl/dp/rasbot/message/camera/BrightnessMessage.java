package pl.dp.rasbot.message.camera;

/**
 * Created by dawidpodolak on 11.08.16.
 */
public class BrightnessMessage extends CameraMessage {

    public BrightnessMessage(Object parameter) {
        super(parameter);
    }

    @Override
    public String getCommand() {
        return "brgnss";
    }


}

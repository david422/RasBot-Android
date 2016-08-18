package pl.dp.rasbot.message;

/**
 * Created by dawidpodolak on 11.08.16.
 */
public abstract class ControllMessage extends Message {

    public ControllMessage(Object parameter) {
        super(parameter);
    }

    @Override
    public String getType() {
        return "control";
    }
}

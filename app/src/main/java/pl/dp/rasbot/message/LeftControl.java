package pl.dp.rasbot.message;

/**
 * Created by dpodolak on 09.08.16.
 */
public class LeftControl extends Message{

    public LeftControl(Object parameter) {
        super(parameter);
    }

    @Override
    public String getCommand() {
        return "left_pwm";
    }
}

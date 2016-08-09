package pl.dp.rasbot.message;

/**
 * Created by dpodolak on 09.08.16.
 */
public class RightControl extends Message{

    public RightControl(Object parameter) {
        super(parameter);
    }

    @Override
    public String getCommand() {
        return "right_pwm";
    }


}

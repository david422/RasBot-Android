package pl.dp.rasbot.message;

/**
 * Created by dpodolak on 09.08.16.
 */
public abstract class Message {

    public abstract String getCommand();

    private Object parameter;

    public Message(Object parameter) {
        this.parameter = parameter;
    }

    public String getJsonString(){
        return String.format("{\"%s\":%s}", getCommand(), parameter);
    }
}

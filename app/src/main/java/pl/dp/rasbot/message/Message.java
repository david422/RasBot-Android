package pl.dp.rasbot.message;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dpodolak on 09.08.16.
 */
public abstract class Message {

    @SerializedName("type")
    public String messageType;

    public abstract String getCommand();
    public abstract String getType();

    private Object parameter;

    public Message(Object parameter) {
        this.parameter = parameter;
    }

    public String getJsonString(){
        return String.format("{\"type\":%s,\"object\":{\"%s\":\"%s\"}}", getType(), getCommand(), parameter);
    }
}

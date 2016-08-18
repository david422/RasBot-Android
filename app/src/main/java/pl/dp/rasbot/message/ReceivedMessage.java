package pl.dp.rasbot.message;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dawidpodolak on 11.08.16.
 */
public class ReceivedMessage{

    @SerializedName("type")
    private String type;

    @SerializedName("object")
    private Object object;

    public ReceivedMessage() {

    }


    public String getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return String.format("type: %s, object: %s", type, object);
    }
}

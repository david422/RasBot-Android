package pl.dp.rasbot.message.camera;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import pl.dp.rasbot.message.Message;

/**
 * Created by dawidpodolak on 11.08.16.
 */
public class Camera1Message extends Message {

    @SerializedName("res")
    private String resolution = "640x380";

    @SerializedName("fps")
    private int fps = 60;

    @SerializedName("brgnss")
    private int brightness = 60;

    @SerializedName("fv")
    private boolean flipVertical = true;

    @SerializedName("fh")
    private boolean flipHorizontal = true;

    public Camera1Message() {
        super(null);
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getType() {
        return "camera";
    }

    @Override
    public String getJsonString() {
        String objectString = new Gson().toJson(this);
        return String.format("{\"type\":%s,\"object\":%s}", getType(), objectString);
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }


    public String getResolution() {
        return resolution;
    }

    public int getFps() {
        return fps;
    }

    public int getBrightness() {
        return brightness;
    }

    public boolean isFlipVertical() {
        return flipVertical;
    }

    public boolean isFlipHorizontal() {
        return flipHorizontal;
    }

    @Override
    public String toString() {
        return String.format("res: %s, fps: %d, brightness: %d, flipVert: %b, flipHor: %b", resolution, fps, brightness, flipVertical, flipHorizontal );
    }
}

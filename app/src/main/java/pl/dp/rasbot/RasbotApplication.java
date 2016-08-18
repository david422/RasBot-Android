package pl.dp.rasbot;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by dpodolak on 10.08.16.
 */
public class RasbotApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}

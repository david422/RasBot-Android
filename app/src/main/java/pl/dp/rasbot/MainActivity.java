package pl.dp.rasbot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.dp.rasbot.connection.ConnectionManager;
import pl.dp.rasbot.utils.BusProvider;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.tvMainActivityStatus)
    TextView mStatusTextView;
    @InjectView(R.id.bMainActivityConnectToServer)
    Button mConnectButton;

    @InjectView(R.id.bMianActivitySterring)
    Button mSterringActivity;

    private ConnectionManager mConnectionManager;

    private Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_activity);
//        GStreamer.init(this);
        ButterKnife.inject(this);

        mConnectionManager = ConnectionManager.getInstance();
        bus = BusProvider.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Subscribe
    public void onGetData(String data){
        switch (data){
            case "Connected":{
                mSterringActivity.setEnabled(true);
            }
        }

    }

    @OnClick(R.id.bMainActivityConnectToServer)
    public void connect(){
        try {

            if (!mConnectionManager.isConnected()){
                mConnectionManager.connect();
                mConnectButton.setText("Rozłącz");
            }else{
                mConnectionManager.close();
                mConnectButton.setText("Połącz z aplikacją Rasbot");
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd połączenia z serwerem!", Toast.LENGTH_LONG).show();
        }
    }



    @OnClick(R.id.bMianActivitySterring)
    public void sterring(){
        startActivity(new Intent(this, SterringActivity.class));
    }
}

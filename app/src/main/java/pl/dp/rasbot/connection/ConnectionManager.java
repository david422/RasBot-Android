package pl.dp.rasbot.connection;

import android.os.Handler;
import android.util.Log;

import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import pl.dp.rasbot.MainActivity;
import pl.dp.rasbot.utils.BusProvider;

/**
 * Created by Project4You S.C. on 02.05.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class ConnectionManager {

    public static final String TAG = "ConnectionManager";
    private static final int IP_PORT = 4333;
    private static final String IP_ADDRESS = "192.168.2.1";

    private Socket wifiSocket;
    private PrintWriter dataPrintWriter;

    private BufferedReader inputBufferedReader;

    private static ConnectionManager mConnectionManagerInstance;
    private Thread communicationThread;
    private Handler handler;

    public static ConnectionManager getInstance() {
        if (mConnectionManagerInstance == null) {
            mConnectionManagerInstance = new ConnectionManager();
        }

        return mConnectionManagerInstance;
    }

    private ConnectionManager() {
    }

    public void connect() throws IOException {
        Thread connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    wifiSocket = new Socket(IP_ADDRESS, IP_PORT);


                    while (!wifiSocket.isConnected());
//                    BusProvider.getInstance().post(MainActivity.ACTION_APPLICATION_CONNECTED);


                    if (handler!= null)
                        handler.sendEmptyMessage(MainActivity.ACTION_APPLICATION_CONNECTED);

                    dataPrintWriter = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(wifiSocket.getOutputStream())),
                            false);

//                    CommunicationThread commThread = new CommunicationThread(wifiSocket);
//                    communicationThread = new Thread(commThread);
//                    communicationThread.start();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        connectionThread.start();
    }

    public void initReader() {

        Thread readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.d(TAG, "while loop");
                        String data = inputBufferedReader.readLine();
                        BusProvider.getInstance().post(data);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        );

        try {
            inputBufferedReader = new BufferedReader(new InputStreamReader(wifiSocket.getInputStream()));
            readerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isConnected(){
        if (wifiSocket==null)
            return false;
        return wifiSocket.isConnected();
    }

    public void close() throws IOException {

        dataPrintWriter.close();
        communicationThread.interrupt();
        wifiSocket.close();

    }

    public void sendMessage(Map<String, String> data) {
        if (dataPrintWriter!= null) {
            Log.d(TAG, "send data:" + JSONValue.toJSONString(data)
                    + ", time:" + (System.currentTimeMillis() / 1000));
            dataPrintWriter.println(JSONValue.toJSONString(data));
            dataPrintWriter.flush();
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
                try {
                    String message = "";
                    int charsRead = 0;
                    char[] buffer = new char[4096];

                    while ((charsRead = input.read(buffer)) != -1) {
                        message += new String(buffer).substring(0, charsRead);
                        Log.d(TAG, "message: " + message);
                        BusProvider.getInstance().post(message);
                    }


                } catch (IOException e) {
                   e.getMessage();
                }
        }

    }

}

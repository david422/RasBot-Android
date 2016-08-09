package pl.dp.rasbot.connection;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import pl.dp.rasbot.message.Message;


/**
 * Created by Project4You S.C. on 02.05.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class ConnectionManager {

    public static final String TAG = "ConnectionManager";

    private Socket messageSocket;
    private PrintWriter dataPrintWriter;

    private int port;
    private String host;

    private MessageCallback messageCallback;

    private Thread communicationThread;

    public ConnectionManager(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public void setMessageCallback(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void connect() {
        Thread connectionThread = new Thread(() -> {

            try {
                messageSocket = new Socket(host, port);

                dataPrintWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(messageSocket.getOutputStream())),
                        false);

                CommunicationThread commThread = new CommunicationThread(messageSocket);
                commThread.setMessageCallback(messageCallback);
                communicationThread = new Thread(commThread);
                communicationThread.start();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        connectionThread.start();
    }


    public boolean isConnected(){
        if (messageSocket ==null)
            return false;
        return messageSocket.isConnected();
    }


    public void sendMessage(Message data) {
        if (dataPrintWriter!= null) {
            Log.d(TAG, "send data:" + data.getJsonString()
                    + ", time:" + (System.currentTimeMillis() / 1000));
            dataPrintWriter.println(data.getJsonString());
            dataPrintWriter.flush();
        }
    }


    public void release() {
        dataPrintWriter.close();
        try {
            messageSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;
        private MessageCallback messageCallback;

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
                    String line;

                    while ((line = input.readLine()) != null) {
                        if (messageCallback != null) {
                            messageCallback.onMessageReceived(line);
                        }
                    }


                } catch (IOException e) {
                   e.getMessage();
                }
        }

        public void setMessageCallback(MessageCallback messageCallback) {
            this.messageCallback = messageCallback;
        }
    }

}

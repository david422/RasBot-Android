package pl.dp.rasbot.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by dawidpodolak on 07.08.16.
 */
public class PingManager implements PingCommandCallback {

    private static final long PING_INTERVAL = 500;
    private static final String PING_COMMAND = "pg";
    private static final int SOCKET_TIMEOUT = 30;

    private Subscription pingSubscription;

    private int port;

    private String host;

    private Socket pingSocket;

    private ArrayBlockingQueue<String> pingQueue = new ArrayBlockingQueue(10);

    private PrintWriter pingPrintWriter;

    private PingCallback pingCallback;

    private PublishSubject<Long> stopSubject = PublishSubject.create();
    private PingReceiver pingReceiver;

    public PingManager(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public void init(){

        Thread pingThread = new Thread(() -> {
            try {
                connectToPingServer();
            } catch (IOException e) {
                pingCallback.connectionError();
                e.printStackTrace();
            }
        });
        pingThread.start();
    }

    public void setPingCallback(PingCallback pingCallback) {
        this.pingCallback = pingCallback;
    }




    private void connectToPingServer() throws IOException {
        pingSocket = new Socket(host, port);

        while (!pingSocket.isConnected());

        if (pingCallback != null) {
            pingCallback.connectionEstablished();
        }

        pingPrintWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(pingSocket.getOutputStream())),
                false);

        pingReceiver = new PingReceiver(pingSocket);
        pingReceiver.setCallback(this);
        pingReceiver.start();


        pingSubscription = Observable.interval(0, PING_INTERVAL, TimeUnit.MILLISECONDS)
                .takeUntil(stopSubject)
                .map(i -> PING_COMMAND)
                .filter(i -> pingQueue.size() <= 2)
                .subscribe(command -> {

                    if (pingQueue.size() > 1){
                        Timber.d("connectToPingServer: pingQueue size: %d", pingQueue.size());
                        release();
                        pingCallback.connectionInterrupted();
                    }
                    Timber.d("PingManager:connectToPingServer: piniging");

                    pingQueue.add("pm");
                }, Throwable::printStackTrace, () -> Timber.d("PingManager:connectToPingServer: onComplete"));

        pingPrintWriter.println(PING_COMMAND);
        pingPrintWriter.flush();


    }

    public void release(){

        stopSubject.onNext(null);
        if (pingSubscription.isUnsubscribed()){
            pingSubscription.unsubscribe();
        }

        try {
            pingSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (pingReceiver != null) {
            pingReceiver.stopReceive();
        }

    }

    public void sendPing(){
        try {
            // TODO: 07.08.16 create unittest for it
            // TODO: 07.08.16 Play with it by testing
            String command = pingQueue.take();

            command += ": sent " + System.currentTimeMillis();

            pingPrintWriter.println(command);
            pingPrintWriter.flush();

//            Timber.d("sendPing: Ping sent");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void pingCommandReceived() {
        sendPing();
    }

    public boolean isConnected() {
        return pingSocket != null && pingSocket.isConnected() && pingQueue.size() < 2;
    }

    public static class PingReceiver extends Thread{

        private PingCommandCallback callback;

        private boolean isRun;

        private BufferedReader pingReader;

        public PingReceiver(Socket pingSocket) throws IOException {

            pingReader = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));
        }

        public void setCallback(PingCommandCallback callback) {
            this.callback = callback;
        }

        @Override
        public synchronized void start() {
            isRun = true;
            super.start();
        }

        public void stopReceive(){
            isRun = false;
        }

        @Override
        public void run() {
            String readCommand;

            try {
                while ((readCommand = pingReader.readLine()) != null && isRun){

                    if (!readCommand.equals(PING_COMMAND)){
                        String sentTime = readCommand.split(" ")[2];
                        long messageSent = Long.parseLong(sentTime);
                        long diff = System.currentTimeMillis() - messageSent;

                    }

                    if (callback != null) {
                        callback.pingCommandReceived();
                    }

                }

                Timber.d("PingReceiver:run: stop pinging");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

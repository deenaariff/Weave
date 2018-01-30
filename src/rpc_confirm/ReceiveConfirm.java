package rpc_confirm;

import ledger.Ledger;
import ledger.Log;

public class ReceiveConfirm {

    private Ledger ledger; // the ledger to append new heart beats to
    private int port;
    private int random_interval;

    // Constructor
    // provide the last_heartbeat object to update
    // ledger to append new logs to
    public ReceiveConfirm(Ledger ledger, int port, int random_interval) {
        this.ledger = ledger;
        this.port = port;
        this.random_interval = random_interval;
    }

    // Ensure that the heart beat is added to the ledger
    public void confirmUpdate(Log heartbeat) {
        ledger.appendToLogs(heartbeat, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void call() throws IOException, ClassNotFoundException {
        ServerSocket listener = new ServerSocket(this.port);
        this.last_heartbeat = System.nanoTime();
        try {
            while (true) {
                if(this.last_heartbeat - System.nanoTime() > this.random_interval) {
                    System.out.println("Randomized Follower Waiting Interval Elapsed");
                    break;
                } else {
                    Socket socket = listener.accept();
                    try {
                        System.out.println("Received a Heartbeat");
                        final InputStream yourInputStream = socket.getInputStream();
                        final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                        final List<Log> heartbeats = (List<Log>) inputStream.readObject();
                        for(Log heartbeat : heartbeats) {
                            //System.out.println("Received new log: " + heartbeat.getKey() + " : " + heartbeat.getValue());
                            updateLedger(heartbeat);
                        }
                        this.last_heartbeat = System.nanoTime();
                    } finally {
                        socket.close();
                    }
                }
            }
        } finally {
            listener.close();
        }
        return null;
    }
}

package rpc_heartbeat;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import messages.HeartBeat;
import routing.RoutingTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This class is used by the Leader, and listens for returning heartbeat
 * messages from its constituents.
 */
public class LeaderListenHeartBeatConfirm implements Callable<Void> {

    private Ledger ledger;
    private RoutingTable rt;
    private HeartBeat hb;
    private HostInfo hostInfo;

    // TODO: Leader maintains a hashmap for each heartbeat?
    // TODO: When item in hashmap reaches a specific number, we can finally commit to logs?

    // Constructor
    public LeaderListenHeartBeatConfirm(Ledger ledger, HostInfo hostInfo) {
        this.ledger = ledger;
        this.hostInfo = hostInfo;
    }

    /**
     * This methods runs the call() method for a Callable.
     *
     * It will listen to Follower HeartBeat confirmations and decrement
     * the HashMap for Logs Accordingly.
     *
     */
    @SuppressWarnings("unchecked")
    public Void call() throws IOException, ClassNotFoundException {
        ServerSocket listener = new ServerSocket(hostInfo.getHeartBeatPort());
        try {
            while (true) {
                Socket socket = listener.accept();
                try {
                    System.out.println("Received a Heartbeat");
                    final InputStream yourInputStream = socket.getInputStream();
                    final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                    final List<Log> confirmations = (List<Log>) inputStream.readObject();
                    for(Log confirmation : confirmations) {
                        //System.out.println("Received new log: " + heartbeat.getKey() + " : " + heartbeat.getValue());
                        ledger.receiveConfirmation(confirmation);
                    }
                } finally {
                    socket.close();
                }
            }
        } finally {
            listener.close();
        }
    }
}

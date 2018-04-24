package rpc_heartbeat;

import info.HostInfo;
import ledger.Ledger;
import messages.HeartBeat;
import routing.RoutingTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * This class is used by the Leader, and listens for returning heartbeat
 * messages from its constituents.
 */
public class LeaderListenHeartBeatConfirm implements Callable<Void> {

    private Ledger ledger;
    private HostInfo host_info;
    private RoutingTable rt;

    public LeaderListenHeartBeatConfirm(Ledger ledger, HostInfo hostInfo, RoutingTable rt) {
        this.ledger = ledger;
        this.host_info = hostInfo;
        this.rt = rt;
    }

    /**
     * This method listens for returning confirmation heartbeats from followers.
     * Once received, it will pass the heartbeat to the ledger so that the
     * ledger can update its commit map and list of logs accordingly
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Void call() throws IOException, ClassNotFoundException {
        System.out.println("[" + this.host_info.getState() + "]: Listening for Returning Heartbeat Messages");
        ServerSocket listener = new ServerSocket(host_info.getHeartBeatPort());

        try {
            // Always listen for incoming heartbeat messages
            while (true) {
                Socket socket = listener.accept();
                try {
                    System.out.println("[" + this.host_info.getState() + "]: Received a Heartbeat");

                    // De-serialize the heartbeat object received
                    final InputStream yourInputStream = socket.getInputStream();
                    final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                    final HeartBeat hb = (HeartBeat) inputStream.readObject();

                    // Notify the ledger that we have received a confirmation heartbeat
                    ledger.receiveConfirmation(hb, rt);
                } finally {
                    socket.close();
                }
            }
        } finally {
            listener.close();
        }
    }
}

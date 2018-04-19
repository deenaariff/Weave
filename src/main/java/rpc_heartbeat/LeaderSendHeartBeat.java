package rpc_heartbeat;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import messages.HeartBeat;
import routing.RoutingTable;
import routing.Route;

/**
 * This class is used by the leader, and periodically sends heartbeat messages
 * to its followers.
 *
 * Messages are sent on a separate thread, and they contain information about
 * the current state of the distributed system.
 */
public class LeaderSendHeartBeat implements Callable<Void> {

	private Ledger ledger;
	private HostInfo host_info;
    private RoutingTable rt;
	private Integer hbInterval = 300;  // milliseconds

    /**
     * Constructor for the LeaderSendHearBeat class
     *
     * @param ledger
     * @param host_info
     * @param rt
     */
	public LeaderSendHeartBeat(Ledger ledger, HostInfo host_info, RoutingTable rt) {
		this.ledger = ledger;
		this.host_info = host_info;
	    this.rt = rt;
	}

    /**
     * This method serializes the heartbeat object and sends it to proper host
     *
     * @param hb
     * @param hostName
     * @param portNumber
     * @throws IOException
     */
	private void send(HeartBeat hb, String hostName, Integer portNumber) throws IOException {
		Socket socket = new Socket(hostName, portNumber);
		final OutputStream outputStream = socket.getOutputStream();
		final ObjectOutputStream output = new ObjectOutputStream(outputStream);
		output.writeObject(hb);
		socket.close();
	}

    /**
     * This method will read from the list of updates that it has received from
     * clients, then create a heartbeat with those updates
     *
     * @return
     */
	private HeartBeat createHeartBeat() {
        List<Log> updates = ledger.getUpdates();
        HeartBeat hb = new HeartBeat(host_info.getTerm(), updates);
        return hb;
    }

    /**
     * This callable sends the heartbeat object to all hosts in the routing table
     *
     * @return
     * @throws Exception
     */
	public Void call() throws Exception {
		while(true) {
			TimeUnit.MILLISECONDS.sleep(hbInterval);

			for (Route host : this.rt.getTable()) {
				try {
					System.out.println("Sending Updates to " + host.getIP() + ":" + host.getHeartbeatPort());
					send(createHeartBeat(), host.getIP(), host.getHeartbeatPort());
				} catch (Exception e) {
					System.out.println("Exception: " + e);
				}
			}
		}
	}
}

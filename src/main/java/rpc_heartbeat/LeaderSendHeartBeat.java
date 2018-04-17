package rpc_heartbeat;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import ledger.Ledger;
import messages.HeartBeat;
import routing.RoutingTable;

/**
 * This class is used by the leader, and periodically sends heartbeat messages
 * to its followers.
 *
 * Messages are sent on a separate thread, and they contain information about
 * the current state of the distributed system.
 */
public class LeaderSendHeartBeat implements Callable<Void> {
	
	private Ledger ledger;
	private RoutingTable rt;
	private HeartBeat hb;
	private Integer hbInterval = 300;  // milliseconds

    /**
     * Constructor for the LeaderSendHearBeat class
     *
     * @param ledger
     * @param rt
     * @param hb heartbeat is loaded with the leader's committed logs
     */
	public LeaderSendHeartBeat(Ledger ledger, RoutingTable rt, HeartBeat hb) {
		this.ledger = ledger;
		this.rt = rt;
		this.hb = hb;
	}

    /**
     * This method serializes the heartbeat object and sends it to proper host
     *
     * @param hb
     * @param hostName
     * @param portNumber
     * @throws IOException
     */
	public void send (HeartBeat hb, String hostName, Integer portNumber) throws IOException {
		Socket socket = new Socket(hostName, portNumber);
		final OutputStream outputStream = socket.getOutputStream();
		final ObjectOutputStream output = new ObjectOutputStream(outputStream);
		output.writeObject(hb);
		socket.close();
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

			for (String host : this.rt.getTable()) {
				try {
					System.out.println("Sending Updates to " + host + " " + this.rt.HEARTBEAT_PORT);
					send(hb, host, rt.HEARTBEAT_PORT);
				} catch (Exception e) {
					System.out.println("Exception: " + e);
				}
			}
		}
	}
}

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
 * This class is designed to send a heartbeat message to all followers. The
 * heartbeat message will be the serialized HeartBeat object found in
 * messages/HeartBeat.java
 *
 * This class will only be implemented by a Leader.
 *
 */
public class LeaderSendHeartBeat implements Callable<Void> {
	
	private Ledger ledger;
	private RoutingTable rt;
	private HeartBeat hb;
	private Integer hbInterval = 150;  // Milliseconds between sending heartbeats

    /**
     * This is the constructor for the LeaderSendHeartBeat class
     *
     * @param ledger The ledger containing all key-value pairs
     * @param rt The routing table containing all nodes in network
     * @param hb The heartbeat maintaining state of committed messages
     */
	public LeaderSendHeartBeat(Ledger ledger, RoutingTable rt, HeartBeat hb) {
		this.ledger = ledger;
		this.rt = rt;
		this.hb = hb;  // TODO: This hb object needs to be passed in from rpc_client
	}

    /**
     * This method serializes the heartbeat object, then sends that serialized
     * object through socket i/o to a specified follower
     *
     * @param hb The non-serialized heartbeat object
     * @param hostName The host name of the follower
     * @param portNumber The port number of the follower
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
     * Sends a serialized heartbeat message to all followers at a specified
     * interval
     *
     * @return
     * @throws Exception
     */
	@Override
	public Void call() throws Exception {
		while(true) {
			TimeUnit.MILLISECONDS.sleep(hbInterval);

			// Send the heartbeat message to all nodes in routing table
			for(String host : this.rt.getTable()) {
				try {
					System.out.println("Sending Updates to " + host + " " + this.rt.HEARTBEAT_PORT);
					send(this.hb, host, rt.HEARTBEAT_PORT);
				} catch (Exception e) {
					System.out.println("Exception: " + e);
				}
			}
		}
	}
}

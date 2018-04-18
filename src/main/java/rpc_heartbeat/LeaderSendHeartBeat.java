package rpc_heartbeat;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import ledger.Ledger;
import ledger.Log;
import routing.RoutingTable;

public class LeaderSendHeartBeat implements Callable<Void> {
	
	private Ledger ledger;
	private RoutingTable rt;
	
	public LeaderSendHeartBeat(Ledger ledger, RoutingTable rt) {
		this.ledger = ledger;
		this.rt = rt;
	}
	
	public void send (List<Log> logs, String hostName, Integer portNumber) throws UnknownHostException, IOException {
		Socket socket = new Socket(hostName, portNumber);
		final OutputStream outputStream = socket.getOutputStream();
		final ObjectOutputStream output = new ObjectOutputStream(outputStream);
		output.writeObject(logs);
		socket.close();
	}

	public Void call() throws Exception {
		while(true) {
			TimeUnit.SECONDS.sleep(1);
			List<Log> updates = this.ledger.getUpdates();
			for(String host : this.rt.getTable()) {
				try {
					System.out.println("Sending Updates to " + host + " " + this.rt.HEARTBEAT_PORT);
					send(updates, host, rt.HEARTBEAT_PORT);
				} catch (Exception e) {
					System.out.println("Exception: " + e);
				}
			}
		}
		//return null;
	}

}

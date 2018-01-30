package rpc_heartbeat;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ledger.Ledger;
import ledger.Log;
import routing.RoutingTable;
import rpc_abstract.SendMessage;

public class SendHeartBeat extends SendMessage {
	
	private Ledger ledger;
	private RoutingTable rt;
	
	public SendHeartBeat(Ledger ledger, RoutingTable rt) {
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

	@Override
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
	
	// Tests 
	public static void main(String [] args) {
		
		Ledger ledger = new Ledger();
		
		RoutingTable rt = new RoutingTable();
		rt.addEntry("127.0.0.1");
		
	    ExecutorService exec = Executors.newFixedThreadPool(3);
	    
	    Callable<Void> callable = new SendHeartBeat(ledger, rt);
		Future<Void> future = exec.submit(callable);
		    
	    try {
	    	future.get();
		} catch (Exception e) {
		    // The exception will be printed out
		    System.out.println("Exception: " + e);
		}
	}

	@Override
	public void send() {
		// TODO Auto-generated method stub
		
	}
	
}

package rpc_heartbeat;

import java.io.IOException;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;

import ledger.Ledger;
import ledger.Log;

// Heart beat thread to listen for RPC from leader
public class FollowerListenHeartBeat implements Callable<Void> {
	
	private Long last_heartbeat; // the time stamp of the last heart beat received
	private Ledger ledger; // the ledger to append new heart beats to 
	private int port;
	private int random_interval;
	
	// Constructor 
	// provide the last_heartbeat object to update
	// ledger to append new logs to
	public FollowerListenHeartBeat(Ledger ledger, int port, int random_interval) {
		this.ledger = ledger;
		this.port = port;
		this.random_interval = random_interval;
	}
	
	// Ensure that the heart beat is added to the ledger
	public void updateLedger(Log heartbeat) {
		ledger.commitToLogs(heartbeat);
	}
	
	@SuppressWarnings("unchecked")
	public Void call() throws IOException, ClassNotFoundException {
	    System.out.println("[Follower]: Listening for Incoming Heartbeat Messages");
		ServerSocket listener = new ServerSocket(this.port);
		this.last_heartbeat = System.nanoTime();
	    try {
	    	while (true) {
	    		if(System.nanoTime() - this.last_heartbeat  > this.random_interval) {
	    			System.out.println("[Follower]: Randomized Follower Waiting Interval Elapsed - Revert to Candidate");
	    			break;
	    		} else {
		    		Socket socket = listener.accept();
		            try {
		            	System.out.println("[Follower]: Received a Heartbeat");
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

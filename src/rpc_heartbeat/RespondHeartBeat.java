package rpc_heartbeat;

import java.io.IOException;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ledger.Ledger;
import ledger.Log;
import rpc_abstract.RespondMessage;

// Heart beat thread to listen for RPC from leader
public class RespondHeartBeat extends RespondMessage {  
	
	private Long last_heartbeat; // the time stamp of the last heart beat received
	private Ledger ledger; // the ledger to append new heart beats to 
	private int port;
	private int random_interval;
	
	// Constructor 
	// provide the last_heartbeat object to update
	// ledger to append new logs to
	public RespondHeartBeat(Ledger ledger, int port, int random_interval) {
		this.ledger = ledger;
		this.port = port;
		this.random_interval = random_interval;
	}
	
	// Ensure that the heart beat is added to the ledger
	public void updateLedger(Log heartbeat) {
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
	
	public static void main(String[] args) {
		
		Ledger ledger = new Ledger();
		
		Callable<Void> callable = new RespondHeartBeat(ledger,8080, 10);
	    ExecutorService exec = Executors.newFixedThreadPool(3);
	    Future<Void> future = exec.submit(callable);
	    
	    try {
	      future.get();
	    } catch (Exception e) {
	      // The exception will be printed out
	      System.out.println("Exception: " + e);
	    }
	    
	}

}

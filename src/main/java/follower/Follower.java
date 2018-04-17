package follower;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import routing.RoutingTable;
import rpc_heartbeat.FollowerListenHeartBeat;
import rpc_heartbeat.LeaderSendHeartBeat;
import state.State;

/** 
 * The Follower Class inherits the abstract class State.
 * <p>
 * It has a public method run() that, when called will listen for heartbeat messages
 * from the leader RAFT node. 
 * <p>
 * It has a randomized time interval which it uses as the amount of time it will wait for the leader node
 * to send a heartbeat before the state changes to "Candidate". In this scenario the run() methods terminates
 * and the the RaftNode Object will invoke the run() method of its member Candidate Object.
 * 
 * @author deenaariff
 *
 */

public class Follower extends State {
	
	private int random_interval;
	private int INTERVAL_MAX = 350; // max interval in milliseconds
	private HostInfo host;
	private ExecutorService exec;

	/**
	* Constructor for Follower
	* 
	* @param ledger A Ledger object that the follower can append new entries to
	* @param host The port to listen on for heartbeat messages
	*/
	public Follower(Ledger ledger, HostInfo host) {
		this.host = host;
		this.ledger = ledger;
		this.random_interval = 0 + (int)(Math.random() * INTERVAL_MAX);
		this.exec = Executors.newFixedThreadPool(3);
	}
	
	/**
	* Second Constructor for Follower to pass in a executor service
	* 
	* @param ledger A Ledger object that the follower can append new entries to
	* @param host The port to listen on for heartbeat message
	* @param exec The Executor Service to run callables and obtain futures
	 * */
	public Follower(Ledger ledger, HostInfo host, ExecutorService exec) {
		this.host = host;
		this.ledger = ledger;
		this.random_interval = 0 + (int)(Math.random() * INTERVAL_MAX);
		this.exec = exec;
	}
	
	/**
	 * The abstract method inherited from the State Abstract Class.
	 * This method is called to start the functionality of the Follower state. 
	 * 
	 */
	@Override
	public int run() {

		// Create a thread to accept heart beats
		Callable<Void> accept_hb = new FollowerListenHeartBeat(this.ledger, this.host.getHeartBeatPort(), this.random_interval);
		Future<Void> future = exec.submit(accept_hb);

		try {
			future.get();
			return 1;
		} catch (Exception e) {
			// The exception will be printed out
			System.out.println("Exception: " + e);
		}

		return 0;

	}
	
	/**
	 * This method returns the member Executor Service Object of the Class.
	 * Can be used to submit a HeartBeat sender thread to test the Follower class.
	 * 
	 * @return The Class's member ExecutorService Object
	 */
	public ExecutorService getExecutor() {
		return this.exec;
	}
	
	/**
	 * Tests for the Follower Class. Creates a mock service that will send heartbeats for a Follower
	 * Object to consume. 
	 * 
	 * @param args
	 */
	/*public static void main(String[] args) {
		
		// Create New Follower
		Ledger ledger = new Ledger();
		HostInfo host = new HostInfo("127.0.0.1");
		Follower f = new Follower(ledger, host);
		
		// Create Mock Ledger to Send New Messages
		Ledger mock_ledger = new Ledger();
		mock_ledger.addToQueue(new Log(1,0,"Brad","Pitt"));
		mock_ledger.addToQueue(new Log(1,1,"George","Clooney"));
		
		f.run();
		
		// Create a MockHeartBeat()
		RoutingTable rt = new RoutingTable();
		rt.addEntry(host.getHostName());	
	    Callable<Void> mhb = new LeaderSendHeartBeat(mock_ledger, rt);
	    ExecutorService exec = f.getExecutor();
		exec.submit(mhb);	
	}*/

}

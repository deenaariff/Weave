package leader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.HostInfo;
import ledger.Ledger;
import routing.RoutingTable;
import rpc_heartbeat.FollowerListenHeartBeat;
import rpc_heartbeat.LeaderSendHeartBeat;
import state.State;

/**
 * The Leader Class inherits the abstract class State.
 * 
 * It has a public method run() that when invoked will send heartbeat messages
 * to all ip's registered in the class's RoutingTable Object.
 * 
 * If the Leader realizes that it is inconsistent with the cluster
 * it will revert back to a follower node, which will result in a leader election in the cluster
 * 
 * @author deenaariff
 *
 */
public class Leader extends State {

	private Ledger ledger;
	private RoutingTable rt;
	private HostInfo host;
	private ExecutorService exec;

	/**
	 * The constructor for the Leader class.
	 * 
	 * @param ledger The ledger to check for updates and by which to broadcast updates via heartbeat messages.
	 * @param list The list of nodes in the cluster, to send Heartbeat messages to. 
	 */
	public Leader(Ledger ledger, RoutingTable rt, HostInfo host) {
		this.ledger = ledger;
		this.rt = rt;
		this.host = host;
		this.exec = Executors.newFixedThreadPool(3);
	}
	
	/**
	 * This is a method to add a new IP address to the Routing Table to send Heartbeat messages to. 
	 * 
	 * @param host The base host name / IP Address of a node in the cluster.
	 * @param port The port to send Heartbeat messages to.
	 */
	public void addHost(String host) {
		this.rt.addEntry(host);
	}
	
	/**
	 * This is a getter Method to return the ExecutorService Object of the Class. 
	 * 
	 * @return The Member ExecutorService Object of the class.
	 */
	public ExecutorService getExecutor () {
		return this.exec;
	}

	/**
	 * This starts the main functionality of the class and creates a SendHeartBeatObject to 
	 * send Heartbeat messages to all hosts in the list.
	 * 
	 */
	@Override
	public int run() {
		Callable<Void> shb = new LeaderSendHeartBeat(this.ledger, this.rt);
		exec.submit(shb);
		return 1;
	}
	
	/**
	 * Tests for the Leader Class.
	 * 
	 * @param args
	 */
	public static void main (String [] args) {
		
		Callable<Void> rhb = new FollowerListenHeartBeat(new Ledger(), 8080, 10);
		
		Ledger ledger = new Ledger();
		HostInfo host = new HostInfo("127.0.0.1");
		
		Leader leader = new Leader(ledger, new RoutingTable(), host);
		leader.addHost("127.0.0.1");
		
		ExecutorService exec = leader.getExecutor();
		exec.submit(rhb);
		
		leader.run();	
	}

}

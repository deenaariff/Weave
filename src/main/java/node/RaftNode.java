package node;
import candidate.Candidate;
import follower.Follower;
import info.HostInfo;
import leader.Leader;
import ledger.Ledger;
import routing.Route;
import routing.RoutingTable;
import state.State;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the class that contains the implementation of the Raft Node and
 * for switching between states. This class passes essential information to 
 * each class that implements various state's logic.
 * 
 * @author deenaariff
 */
public class RaftNode {

    private final Integer THREAD_COUNT = 3;
	
	private Ledger ledger;
	private HostInfo host;
	private RoutingTable rt;
	private State leader;
	private State follower;
	private State candidate;
	private ExecutorService exec;
	
	/**
	 * The constructor for the RaftNode Class
	 * 
	 */
	public RaftNode(Ledger ledger, Route route) {
		this.ledger = ledger;
		this.host = new HostInfo(route);
		this.rt = new RoutingTable();
		this.follower = new Follower(this.ledger,this.host);
		this.candidate = new Candidate(this.ledger, this.rt, this.host);
		this.leader = new Leader(this.ledger, this.rt, this.host);
        this.exec = Executors.newFixedThreadPool(THREAD_COUNT);
	}

	/**
	 * Second constructor for the RaftNode Class
     * Pass option Routing Table
	 *
	 */
	public RaftNode(RoutingTable rt,  Ledger ledger, Route route) {
		this.ledger = ledger;
		this.host = new HostInfo(route);
		this.rt = rt;
		this.follower = new Follower(this.ledger,this.host);
		this.candidate = new Candidate(this.ledger, this.rt, this.host);
		this.leader = new Leader(this.ledger, this.rt, this.host);
        this.exec = Executors.newFixedThreadPool(THREAD_COUNT);
	}

	
	/**
	 * Runs the Node in the leader state
	 * 
	 */
	public void runLeader() {
		System.out.println("[" + host.getState() + "]: Entering Leader State");

		int result = leader.run();
		if(result == 1) {
			host.becomeFollower();
		} 
	}
	
	/**
	 * Runs the Node in the Candidate State
	 * 
	 */
	public void runCandidate() {
        System.out.println("[" + host.getState() + "]: Entering Candidate State");
		int result = candidate.run();
		if(result == 1) {
			host.becomeLeader();
		} else {
			host.becomeFollower();
		}
	}
	
	/**
	 * Runs the node in the Follower State
	 * 
	 */
	public void runFollower() {
        System.out.println("[" + host.getState() + "]:Entering Follower State");
		follower.run();
		
		/// increment the term before becoming a candidate
		host.incrementTerm();
		host.becomeCandidate();
	}
	
	
	/***
	 * Returns the Node's Ledger
	 * 
	 * @return
	 */
	public Ledger getLedger() {
		return ledger;
	}
	
	/**
	 * Return's the nodes current state
	 * 
	 * @return
	 */
	public HostInfo getHostInfo() {
		return host;
	}
	
}

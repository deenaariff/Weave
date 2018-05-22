package info;

import ledger.Ledger;
import routing.RoutingTable;
import rpc.rpc;
import voting_booth.VotingBooth;
import routing.Route;

import java.io.Serializable;

/**
 * The info wrapper that a host can use to let all subclasses gain access to host info
 * 
 */
public class HostInfo implements Serializable {


    // TODO: Save heartbeat_port and voting_port to env var. Read from env var on startup
	private final Route route;
	private Integer term;
	private String state;

	private Boolean hasVoted;
	private Route votedFor;
	private Boolean initialized;

    private final static int HEARTBEAT_INTERVAL = 50;
    private final static int HEARTBEAT_TIMEOUT_MIN = 150;
    private final static int HEARTBEAT_TIMEOUT_MAX = 2000;

    private int hearbeat_timeout_interval;

    private final String FOLLOWER_TAG = "FOLLOWER";
	private final String CANDIDATE_TAG = "CANDIDATE";
	private final String LEADER_TAG = "LEADER";

	private int votes_obtained;
	
	/**
	 * Constructor for the HostInfo class
	 * 
	 * @param route All Relevant routing information for the host
	 *
	 */
	public HostInfo(Route route) {
		this.route = route;
		this.term = 0;
		this.votedFor = null;
		this.initialized = false;
        this.becomeFollower();
        this.votes_obtained = 0;
        this.hasVoted = false;
	}

	public boolean isFollower() {
		return this.state == FOLLOWER_TAG;
	}
	public boolean isCandidate() {
		return this.state == CANDIDATE_TAG;
	}
	public boolean isLeader() {
		return this.state == LEADER_TAG;
	}

	/**
	 * Sets state_helpers to a leader
	 * 
	 */
	public void becomeFollower() {
	    this.hearbeat_timeout_interval = HEARTBEAT_TIMEOUT_MIN + (int)(Math.random() * HEARTBEAT_TIMEOUT_MAX);
	    this.state = FOLLOWER_TAG;
	    this.initialized = false;
	    this.hasVoted = false;
        System.out.println("[" + this.state + "]: Entered Follower State");
	}

	
	/**
	 * Transitions state of node to become a Candidate. It also sets the
	 * intialized flag to false which will allow it to execute its
     * initialization code only once later.
	 *
	 * This method will also trigger the voting booth's startElection() method.
	 */
	public void becomeCandidate(VotingBooth vb, RoutingTable rt, Ledger ledger) {
	    this.state = CANDIDATE_TAG;
	    vb.startElection();
		System.out.println("[" + this.state + "]: Entered Candidate State");
		System.out.println("[" + this.getState() + "]: Requesting Votes from Followers");
		rpc.broadcastVotes(rt,this, ledger);
	}

	/**
	 * Transitions state of node to become a Leader. It also sets the
     * initialized flag to false which will allow it to execute its
     * initialization code only once later.
	 *
     * Once the state has changed, the main thread will recognize the node to be
     * a leader and begin broadcasting AppendEntries RPCs.
	 */
	public void becomeLeader() {
	    this.state = LEADER_TAG;
	    this.initialized = false;
        System.out.println("[" + this.state + "]: Entered Leader State");
	}

	/**
	 * Is the route parameter the same as the member variable route
	 *
	 * @param route
	 * @return
	 */
	public boolean matchRoute(Route route) {
		return this.route.equals(route);
	}

	/**
	 * Increment current term of the hostInfo object
	 *
	 */
	public void incrementTerm() {
		this.term += 1;
	}
	

	/** Getters and Setters */

	public Integer getId() { return this.route.getId(); }

	public String getHostName() { return this.route.getIP(); }

	public int getEndPointPort() { return this.route.getEndpointPort(); }

	public int getHeartBeatPort() { return this.route.getHeartbeatPort(); }

	public int getVotingPort() { return this.route.getVotingPort(); }

	public Integer getTerm() { return term; }

	public void setTerm(Integer new_term) { this.term = new_term; }

	public String getState() { return this.state; }

    public Route getRoute() { return this.route; }

    public static int getHeartbeatInterval() { return HEARTBEAT_INTERVAL; }

    public int getHeartbeatTimeoutInterval() { return this.hearbeat_timeout_interval; }

    public boolean isInitialized() { return this.initialized; }

    public boolean hasVoted() { return this.votedFor != null; }

    public boolean voteFlag() { return this.hasVoted; };

	public void setVoteFlag(boolean flag) { this.hasVoted = flag; }

    public void setVote(Route route) { this.votedFor = route; }

    public void setVotesObtained(int votes) { this.votes_obtained = votes; }

    public Integer getVotesObtained () { return this.votes_obtained; }

}



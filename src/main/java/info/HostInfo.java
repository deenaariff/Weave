package info;


import routing.RoutingTable;
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
    private final static int ELECTION_INTERVAL = 200;
    private final static int HEARTBEAT_TIMEOUT_MIN = 150;
    private final static int HEARTBEAT_TIMEOUT_MAX = 300;

    private int hearbeat_timeout_interval;

    private final String FOLLOWER_TAG = "FOLLOWER";
	private final String CANDIDATE_TAG = "CANDIDATE";
	private final String LEADER_TAG = "LEADER";

	private VotingBooth vb;
	private RoutingTable rt;
	
	/**
	 * Constructor for the HostInfo class
	 * 
	 * @param route All Relevant routing information for the host
	 *
	 */
	public HostInfo(Route route, RoutingTable rt) {
		this.route = route;
		this.term = 0;
		this.votedFor = null;
		this.initialized = false;
		this.rt = rt;
        this.becomeFollower();
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
     * Obtain id of node
     *
     */
    public Integer getId() {
        return this.route.getId();
    }

	
	/**
	 * Obtain the host IP of the port
	 * 
	 * @return the HOST_NAME of the port
	 */
	public String getHostName() {
	    return this.route.getIP();
	}

	/**
	 * Obtain the heartBeat listening port of the node
	 * 
	 * @return the HEARTBEAT_PORT value
	 */
	public Integer getHeartBeatPort() {
	    return this.route.getHeartbeatPort();
	}
	
	/**
	 * Obtain the voting listening port of the node
	 * 
	 * @return the VOTING_PORT value
	 */
	public Integer getVotingPort() {
		return this.route.getVotingPort();
	}

	/**
	 * Determines if there is a term conflict with another term
	 * 
	 * @param new_term the term to check against
	 * @return true if current term is behind, false otherwise
	 */
	public boolean isTermConflict(Integer new_term) {
	    return (new_term > this.term);
	}

	/**
	 * Get the current term of the host
	 *
	 * @return the value of the term
	 */
	public Integer getTerm() { return term; }

	/**
	 * Update current term of the hostInfo object
	 * 
	 * @param new_term the term to update the current term to
	 */
	public void setTerm(Integer new_term) {
	    this.term = new_term;
	}
	
	/**
	 * Increment current term of the hostInfo object
	 *
	 */
	public void incrementTerm() {
	    this.term += 1;
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
	 * Checks if state_helpers is a rpc
	 * 
	 * @return true if rpc
	 */
	public boolean isFollower() {
	    return this.state == FOLLOWER_TAG;
	}
	
	/**
	 * Sets state_helpers to candidate
	 * 
	 */
	public void becomeCandidate() {
	    this.state = CANDIDATE_TAG;
	    this.initialized = false;
	    this.vb.startElection();
        System.out.println("[" + this.state + "]: Entered Candidate State");
	}
	
	/**
	 * Checks if state_helpers is candidate
	 * 
	 * @return true if candidate
	 */
	public boolean isCandidate() {
	    return this.state == CANDIDATE_TAG;
	}
	
	/**
	 * Sets state_helpers to leader
	 * 
	 */
	public void becomeLeader() {
	    this.state = LEADER_TAG;
	    this.initialized = false;
	    //this.rt.syncNextIndex();
        System.out.println("[" + this.state + "]: Entered Leader State");
	}
	
	/**
	 * Checks if state_helpers is in leader
	 * 
	 * @return true if leader
	 */
	public boolean isLeader() {
	    return this.state == LEADER_TAG;
	}
	
	/**
	 * Returns the current state_helpers;
	 * 
	 * @return the current state_helpers
	 */
	public String getState() {
		return this.state;
	}

    public void setVotingBooth(VotingBooth vb) { this.vb = vb; }

    public Route getRoute() { return route; }

    public static int getHeartbeatInterval() { return HEARTBEAT_INTERVAL; }

    public static int getElectionInterval() { return ELECTION_INTERVAL; }

    public int getHeartbeatTimeoutInterval() { return this.hearbeat_timeout_interval; }

    public boolean isInitialized() { return this.initialized; }

    public void hasBeenInitialized() { this.initialized = true; }

    public boolean hasVoted() { return this.votedFor != null; }

    public void setVote(Route route) { this.votedFor = route; }

}



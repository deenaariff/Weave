package info;

import logger.Logger;
import ledger.Ledger;
import routing.RoutingTable;
import rpc.rpc;
import voting_booth.VotingBooth;
import routing.Route;

/**
 *
 */
public class HostInfo {

	private final Logger logger;

	private final Route route;
	private Integer term;
	private String state;

	private Boolean hasVoted;
	private Route votedFor;

    private final static int HEARTBEAT_INTERVAL = 1000;
    private final static int HEARTBEAT_TIMEOUT_MIN = 2000;
    private final static int HEARTBEAT_TIMEOUT_MAX = 3000;

    private int heartbeat_timeout_interval;

    private final String FOLLOWER_TAG = "FOLLOWER";
	private final String CANDIDATE_TAG = "CANDIDATE";
	private final String LEADER_TAG = "LEADER";

	private int votes_obtained;


	/**
	 * Constructor for the HostInfo class
	 * 
	 * @param route All Relevant routing information for the host
	 */
	public HostInfo(Route route) {
		this.route = route;
		this.term = 0;
		this.votedFor = null;
        this.votes_obtained = 0;
        this.hasVoted = false;
        this.logger = new Logger(this);
		this.becomeFollower();
	}

	/**
	 * Simulate a node crash by restarting
	 */
	public void restart() {
		this.term = 0;
		this.votedFor = null;
		this.votes_obtained = 0;
		this.hasVoted = false;
		this.becomeFollower();
	}


	/**
	 *
	 * @return returns Boolean based on whether state is Follower
	 */
	public boolean isFollower() {
		return this.state == FOLLOWER_TAG;
	}

	/**
	 *
	 * @return returns Boolean based on whether state is Candidate
	 */
	public boolean isCandidate() {
		return this.state == CANDIDATE_TAG;
	}

	/**
	 *
	 * @return returns Boolean based on whether state is Leader
	 */
	public boolean isLeader() {
		return this.state == LEADER_TAG;
	}

	/**
	 * Sets state_helpers to a leader
	 */
	public void becomeFollower() {
	    this.heartbeat_timeout_interval = HEARTBEAT_TIMEOUT_MIN + (int)(Math.random() * (HEARTBEAT_TIMEOUT_MAX- HEARTBEAT_TIMEOUT_MIN));
	    this.state = FOLLOWER_TAG;
	    this.hasVoted = false;
        logger.log("[" + this.state + "]: Entered Follower State");
	}

	/**
	 * Transitions state of node to become a Candidate. It also sets the
	 * intialized flag to false which will allow it to execute its
     * initialization code only once later.
	 *
	 * This method will also trigger the voting booth's startElection() method.
	 *
	 * @param vb The {@link VotingBooth} Object to start new election
	 * @param rt The {@link RoutingTable} Object to broadcast new votes
	 * @param ledger The {@link Ledger} Object to determine info in Votes
	 */
	public void becomeCandidate(VotingBooth vb, RoutingTable rt, Ledger ledger) {
	    this.state = CANDIDATE_TAG;
	    vb.startElection();
		logger.log("[" + this.state + "]: Entered Candidate State");
		logger.log("[" + this.getState() + "]: Requesting Votes from Followers");
		rpc.broadcastVotes(rt,this, ledger);
	}

	/**
	 * Transitions state of node to become a Leader. It also sets the
     * initialized flag to false which will allow it to execute its
     * initialization code only once later.
	 *
     * Once the state has changed, the main thread will recognize the node to be
     * a leader and begin broadcasting AppendEntries RPCs.
	 *
	 * @param rt The {@link RoutingTable} Object to
	 */
	public void becomeLeader(RoutingTable rt) {
	    this.state = LEADER_TAG;
        logger.log("[" + this.state + "]: Entered Leader State");
        rpc.notifyElectionChange(this, rt);
	}

	/**
	 * Is the route parameter the same as the member variable route
	 *
	 * @param route The {@link Route} to match against this node's route
	 * @return Return boolean based on whether provided route mathces this node's route
	 */
	public boolean matchRoute(Route route) {
		return this.route.equals(route);
	}

	/**
	 *
	 * Increment current term of the hostInfo object
	 */
	public void incrementTerm() {
		this.term += 1;
	}


	/**
	 *
	 * @return returns int based on unique _id of route of node
	 */
	public int getId() {
		return this.route.getId();
	}

	/**
	 *
	 * @return returns String based on IP address of route of node
	 */
	public String getHostName() {
		return this.route.getIP();
	}

	/**
	 *
	 * @return returns int based on endpoint port in route of node
	 */
	public int getEndPointPort() {
		return this.route.getEndpointPort();
	}

	/**
	 *
	 * @return returns int based on heartbeat port in route of node
	 */
	public int getHeartBeatPort() {
		return this.route.getHeartbeatPort();
	}

	/**
	 *
	 * @return returns int based on voting port in route of node
	 */
	public int getVotingPort() {
		return this.route.getVotingPort();
	}

	/**
	 *
	 * @return int based on term of this node
	 */
	public int getTerm() {
		return this.term;
	}

	/**
	 * Sets the new term of a node. Usually, called by follower in
	 * term sychronization during log replication process.
	 *
	 * @param new_term the new term to update this node to
	 */
	public void setTerm(Integer new_term) {
		this.term = new_term;
	}

	/**
	 *
	 * @return returns String based on current state (follower,leader,candidate) of node
	 */
	public String getState() {
		return this.state;
	}

	/**
	 *
	 * @return returns {@link Route} based on route of this node
	 */
    public Route getRoute() {
    	return this.route;
    }

	/**
	 *
	 * @return returns int based on interval between Heartbeat messages
	 */
	public static int getHeartbeatInterval() {
		return HEARTBEAT_INTERVAL;
	}

	/**
	 *
	 * @return returns int based on randomized timeout interval for a follower before becoming a candidate
	 */
    public int getHeartbeatTimeoutInterval() {
    	return this.heartbeat_timeout_interval;
    }

	/**
	 *
	 * @return returns boolean based on  whether this node has voted for another node yet
	 */
	public boolean hasVoted() {
		return this.votedFor != null;
	}

	/**
	 *
	 * @return boolean based on whether
	 */
    public boolean voteFlag() {
    	return this.hasVoted;
    };

	/**
	 *
	 * @param flag The Flag to tell whether this node has voted
	 */
	public void setVoteFlag(boolean flag) {
		this.hasVoted = flag;
	}

	/**
	 *
	 * @param route The {@link Route} of the node that this node has voted for
	 */
    public void setVote(Route route) {
    	this.votedFor = route;
    }

	/**
	 *
	 * @param votes The number of votes a node has obtained in an election
	 */
	public void setVotesObtained(int votes) {
		this.votes_obtained = votes;
	}

	/**
	 *
	 * @return returns the number of votes obtained during an election
	 */
    public Integer getVotesObtained () {
    	return this.votes_obtained;
    }

	/**
	 *
	 * @return returns {@link Logger} based upon logger set within this instantiation
	 */
	public Logger getLogger() {
    	return this.logger;
	}

}



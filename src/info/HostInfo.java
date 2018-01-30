package info;

/**
 * The info wrapper that a host can use to let all subclasses gain access to host info
 * 
 */
public class HostInfo {
	
	// TODO: Save heartbeat_port and voting_port to env var. Read from env var on startup
	private final String HOST_NAME;
	private final Integer HEARTBEAT_PORT;
	private final Integer VOTING_PORT;
	private Integer term;
	private String state; 
	
	private final String FOLLOWER_TAG = "FOLLOWER";
	private final String CANDIDATE_TAG = "CANDIDATE";
	private final String LEADER_TAG = "LEADER";
	
	private final Integer DEFAULT_HEARTBEAT_PORT = 8080;
	private final Integer DEFAULT_VOTING_PORT = 8081;
	
	/**
	 * Constructor for the HostInfo class
	 * 
	 * @param HOST_NAME Default Host IP of the host
	 * @param hostPort Default port of the host
	 */
	public HostInfo(String hostName) {
		this.HOST_NAME = hostName;
		this.HEARTBEAT_PORT = DEFAULT_HEARTBEAT_PORT;
		this.VOTING_PORT = DEFAULT_VOTING_PORT;
		this.term = 0;
		this.state = FOLLOWER_TAG;
	}
	
	/**
	 * Constructor for the HostInfo class
	 * 
	 * @param HOST_NAME Default Host IP of the host
	 * @param hostPort Default port of the host
	 */
	public HostInfo(String hostName, Integer heartbeatPort, Integer votingPort) {
		this.HOST_NAME = hostName;
		this.HEARTBEAT_PORT = heartbeatPort;
		this.VOTING_PORT = votingPort;
		this.term = 0;
		this.state = FOLLOWER_TAG;
	}
	
	/**
	 * Second constructor for the HostInfo class
	 * 
	 * @param HOST_NAME Default Host IP of the host
	 * @param hostPort Default port of the host
	 */
	public HostInfo(String hostName, Integer heartbeatPort, Integer votingPort, Integer term) {
		this.HOST_NAME = hostName;
		this.HEARTBEAT_PORT = heartbeatPort;
		this.VOTING_PORT = votingPort;
		this.term = term;
		this.state = FOLLOWER_TAG;
	}
	
	/**
	 * Obtain the host IP of the port
	 * 
	 * @return the HOST_NAME of the port
	 */
	public String getHostName() {
		return this.HOST_NAME;
	}

	/**
	 * Obtain the heartBeat listening port of the node
	 * 
	 * @return the HEARTBEAT_PORT value
	 */
	public Integer getHeartBeatPort() {
		return this.HEARTBEAT_PORT;
	}
	
	/**
	 * Obtain the voting listening port of the node
	 * 
	 * @return the VOTING_PORT value
	 */
	public Integer getVotingPort() {
		return this.VOTING_PORT;
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
	 * @param new_term the term to update the current term to
	 */
	public void incrementTerm() {
		this.term += 1;
	}
	
	/**
	 * Sets state to a leader
	 * 
	 */
	public void becomeFollower() {
		this.state = FOLLOWER_TAG;
	}
	
	/**
	 * Checks if state is a follower
	 * 
	 * @return true if follower
	 */
	public boolean isFollower() {
		return this.state == FOLLOWER_TAG;
	}
	
	/**
	 * Sets state to candidate
	 * 
	 */
	public void becomeCandidate() {
		this.state = CANDIDATE_TAG;
	}
	
	/**
	 * Checks if state is candidate
	 * 
	 * @return true if candidate
	 */
	public boolean isCandidate() {
		return this.state == CANDIDATE_TAG;
	}
	
	/**
	 * Sets state to leader
	 * 
	 */
	public void becomeLeader() {
		this.state = LEADER_TAG;
	}
	
	/**
	 * Checks if state is in leader
	 * 
	 * @return true if leader
	 */
	public boolean isLeader() {
		return this.state == LEADER_TAG;
	}
	
	/**
	 * Returns the current state;
	 * 
	 * @return the current state 
	 */
	public String getState() {
		return this.state;
	}
}



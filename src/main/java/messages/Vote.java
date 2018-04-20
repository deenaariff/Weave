package messages;

import java.io.Serializable;

import info.HostInfo;

/**
 * This class is designed to be serialized and sent across a network. Therefore, it implements Serializable.
 * 
 * The Vote Object will be instantiated and sent to all Nodes available in a Candidate's Routing Table. 
 * The Follower will then modify the Vote Object, to signify that a vote has been cast for a candidate. 
 * 
 * @author deenaariff
 *
 */
public class Vote implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean isVoteCast;
	private HostInfo host;
	
	/**
	 * The Constructor for the Vote Class.
	 * 
	 * @param host The host of the origin that is requesting the Vote.
	 */
	public Vote(HostInfo host) {
		this.isVoteCast = false;
		this.host = host;
	}
	
	/**
	 * The method a follower uses to cast a vote for a Candidate. 
	 */
	public void castVote() {
		this.isVoteCast = true;
	}
	
	/**
	 * This return's the vote status of the Vote Object
	 */
	public boolean getVoteStatus () {
		return this.isVoteCast;
	}
	
	/**
	 * This returns the vote's origin host
	 */
	public String getHost() {
		return this.host.getHostName();
	}
	
	/**
	 * This return's the vote's origin hosts' port that is being listened on
	 */
	public int getHostPort() {
		return this.host.getVotingPort();
	}

}

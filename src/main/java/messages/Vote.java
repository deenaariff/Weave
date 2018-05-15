package messages;

import java.io.Serializable;

import info.HostInfo;
import routing.Route;

/**
 * This class is designed to be serialized and sent across a network. Therefore, it implements Serializable.
 * 
 * The Vote Object will be instantiated and sent to all Nodes available in a Candidate's Routing Table. 
 * The rpc will then modify the Vote Object, to signify that a vote has been cast for a candidate.
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
	private String ip;
	private int voting_port;
	private int endpoint_port;
    private Route route;
    private int term;
	
	/**
	 * The Constructor for the Vote Class.
	 * 
	 * @param host The host of the origin that is requesting the Vote.
	 */
	public Vote(HostInfo host) {
		this.isVoteCast = false;
		this.ip = host.getHostName();
		this.voting_port = host.getVotingPort();
		this.endpoint_port = host.getEndPointPort();
		this.route = host.getRoute();
		this.term = host.getTerm();
	}

    public Route getRoute() {
        return route;
    }

    /**
	 * The method a rpc uses to cast a vote for a Candidate.
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

	public int getEndpointPort() { return this.endpoint_port; };
	
	/**
	 * This returns the vote's origin host
	 */
	public String getHostName() {
		return this.ip;
	}

	/**
	 * This return's the vote's origin hosts' port that is being listened on
	 */
	public int getVotingPort() {
		return this.voting_port;
	}

    /**
     *
     */
    public int getTerm() { return this.term; }

}

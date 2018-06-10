package messages;

import java.io.Serializable;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
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

	private boolean isVoteCast;
	private String ip;
	private int voting_port;
	private int endpoint_port;
    private Route route;
    private int term;
    private int responder;
    private int last_log_index;
    private int last_log_term;
	
	/**
	 * The Constructor for the Vote Class.
	 * 
	 * @param host The host of the origin that is requesting the Vote.
	 */
	public Vote(HostInfo host, Ledger ledger) {
		this.isVoteCast = false;
		this.ip = host.getHostName();
		this.voting_port = host.getVotingPort();
		this.endpoint_port = host.getEndPointPort();
		this.route = host.getRoute();
		this.term = host.getTerm();
		Log last_log = ledger.getLastLog();

		if(last_log == null) {
			this.last_log_index = 0;
			this.last_log_term = host.getTerm();
		} else {
			this.last_log_index = last_log.getIndex();
			this.last_log_term = last_log.getTerm();
		}
	}

    public Route getRoute() {
        return route;
    }

    /**
	 * The method a rpc uses to cast a vote for a Candidate.
	 * @param id the id of the node to cast a vote for
	 */
	public void castVote(int id) {
		this.isVoteCast = true;
		this.responder = id;
	}

	/**
	 *
	 * @return Returns the last Log Index of the given vote
	 */
	public int getLastLogIndex() {
		return this.last_log_index;
	}

	/**
	 *
	 * @return Returns the {@link Log} last Log Term of the given vote
	 */
	public int getLastLogTerm() {
		return this.last_log_term;
	}

	/**
	 *
	 * @return Returns the integer responder id of a given Vote
	 */
	public int getResponder() {
		return this.responder;
	}

    /**
     *
     * @return returns a Boolean based on whether the vote has been cast
     */
	public boolean getVoteStatus () {
		return this.isVoteCast;
	}

	/**
	 *
	 * @return returns the integer endpoint port of the vote initiator
	 */
	public int getEndpointPort() { return this.endpoint_port; };

	/**
	 *
	 * @return Returns the String hostname of the initiator
	 */
	public String getHostName() {
		return this.ip;
	}

	/**
	 *
	 * @return returns the integer voting Port of the initiator
	 */
	public int getVotingPort() {
		return this.voting_port;
	}

	/**
	 *
	 * @return returns the vote's origin host's term
	 */
	public int getTerm() { return this.term; }

}

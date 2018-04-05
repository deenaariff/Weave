package messages;

import ledger.Log;

import java.io.Serializable;
import java.util.List;

/**
 * Heartbeat is a class that implements Serializable.
 *
 * It represents an message that is passed from the leader to all followers and
 * performs two functions:
 *    1. Followers need to receive regular heartbeats from the leader to know
 *       that the leader is still alive
 *    2. Heartbeats carry the logs which will be written into the distributed
 *       ledger
 *
 */
public class HeartBeat implements Serializable {

	private int term;
	private List<Log> commits;
	private boolean acknowledged;
	private static final long serialVersionUID = 1L;  // TODO: Double check what this does

	/**
	 * Constructor for the Heartbeat class
	 * 
	 * @param term The term of the Heartbeat being instantiated.
	 * @param index The index of the Heartbeat being instantiated.
	 * @param commits The list of logs that have been committed on this node
	 * @param precommits The list of logs that have not yet been committed on
	 *                      this node
	 */
	public HeartBeat(int term, int index, List<Log> commits, List<Log> precommits) {
		this.term = term;
		this.commits = commits;
		this.acknowledged = false;
	}

	/**
	 * Return the current term of heartbeat message
	 *
	 * @return The term member variable of the heartbeat
	 */
	public int getTerm() { return term; }

	/**
	 *
	 *
	 * @return
	 */
	public List<Log> getCommits() { return commits; }

	/**
	 *
	 * @return
	 */
	public static long getSerialVersionUID() { return serialVersionUID; }

	/**
	 *
	 * @return
	 */
	public boolean isAcknowledged() { return acknowledged; }

	/**
	 *
	 * @param acknowledged
	 */
	public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}

/**
 * Notes:
 *
 * Third party client sends message to leader.
 *
 * Leader queues information up, and on the next interval,
 * sends information bundled up in a heartbeat
 *
 * Follower receives heartbeat, updates its own lists of commits and precommits,
 * then sends the heartbeat back to the leader.
 *
 * Leader hashes serialized heartbeat to a commit map. Every time it sends out
 * another heartbeat, it checks to see whether a majority of previous messages
 * have been received.
 *
 * Once majority have sent back the heartbeat the leader initially sent, the
 * leader commits this message from the third-party client.
 *
 * Leader then sends an acknowledgement to the third-party client
 */

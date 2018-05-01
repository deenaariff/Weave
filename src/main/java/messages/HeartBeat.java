package messages;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import routing.Route;
import routing.RoutingTable;

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

    private static final int HEARTBEAT_CAPACITY = 2;

	private int term;  // The term of the sender of the heartbeat
    private int leaderCommitIndex;

    private int prevLogIndex;
    private Log prevLog;

	private List<Log> entries;  // The leader's committed logs
    
	private Boolean reply;  // TODO: Is this still needed? Technically when a heartbeat is sent back, it is acknowledged
	private static final long serialVersionUID = 1L;  // TODO: Double check what this does
	private Route route;


    /**
     *
     * @param hostInfo
     * @param commits
     * @param destination
     * @param rt
     * @param ledger
     */
	public HeartBeat(HostInfo hostInfo, List<Log> commits, Route destination, RoutingTable rt, Ledger ledger) {
		this.term = hostInfo.getTerm();
		this.entries = commits;
		this.reply = null;
		this.route = hostInfo.getRoute();
        this.prevLogIndex = rt.getNextIndex(destination);
        this.prevLog = ledger.getLogbyIndex(this.prevLogIndex);
        this.leaderCommitIndex = ledger.getCommitIndex();
	}

    public static int getHeartbeatCapacity() { return HEARTBEAT_CAPACITY; }

    public Route getRoute() { return route; }

	public void setRoute(Route route) { this.route = route; }

	public int getTerm() { return term; }

    public int getLeaderCommitIndex() { return leaderCommitIndex; }

    public void setTerm(int new_term) { this.term = new_term; }

	public List<Log> getEntries() { return entries; }

	public static long getSerialVersionUID() { return serialVersionUID; }

    public int getPrevLogIndex() { return prevLogIndex; }

    public void setPrevLogIndex(int prevLogIndex) { this.prevLogIndex = prevLogIndex; }

    public Log getPrevLog() { return prevLog; }

    public void setPrevLog(Log prevLog) { this.prevLog = prevLog; }

    public boolean hasReplied() { return this.reply != null; }

	public Boolean getReply () { return this.reply; }

	public void setReply(boolean response) { this.reply = response; }

}

/**
 * Notes:
 *
 * Third party client sends message to leader.
 *
 * Leader queues information up, and on the next interval,
 * sends information bundled up in a heartbeat
 *
 * rpc receives heartbeat, updates its own lists of entries and precommits,
 * then sends the heartbeat back to the leader.
 *
 * Leader hashes serialized heartbeat to a commit map. Every time it sends out
 * another heartbeat, it checks to see whether a majority of previous messages
 * have been received.
 *
 * Once majority have sent back the heartbeat the leader initially sent, the
 * leader entries this message from the third-party client.
 *
 * Leader then sends an acknowledgement to the third-party client
 */

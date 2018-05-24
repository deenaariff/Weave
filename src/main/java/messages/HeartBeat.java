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
    
	private Boolean reply;
	private static final long serialVersionUID = 1L;  // TODO: Double check what this does
	private Route originRoute;
	private Route responderRoute;


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
		this.originRoute = hostInfo.getRoute();
		this.responderRoute = destination;
        this.prevLogIndex = rt.getNextIndex(destination) - 1;
        this.prevLog = ledger.getLogbyIndex(this.prevLogIndex);
        this.leaderCommitIndex = ledger.getCommitIndex();
	}

    public static int getHeartbeatCapacity() { return HEARTBEAT_CAPACITY; }

    public int getTerm() { return term; }

    public int getLeaderCommitIndex() { return leaderCommitIndex; }

    public void setTerm(int new_term) { this.term = new_term; }

	public List<Log> getEntries() { return entries; }

    public int getPrevLogIndex() { return prevLogIndex; }

    public void setPrevLogIndex(int prevLogIndex) { this.prevLogIndex = prevLogIndex; }

    public Log getPrevLog() { return prevLog; }

    public void setPrevLog(Log prevLog) { this.prevLog = prevLog; }

    public boolean hasReplied() { return this.reply != null; }

	public Boolean getReply () { return this.reply; }

	public void setReply(boolean response) { this.reply = response; }

    public Route getOriginRoute() { return originRoute; }

    public void setOriginRoute(Route originRoute) { this.originRoute = originRoute; }

    public Route getResponderRoute() { return responderRoute; }

    public void setResponderRoute(Route responderRoute) { this.responderRoute = responderRoute; }
}

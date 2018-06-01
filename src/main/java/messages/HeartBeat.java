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

    private static final long serialVersionUID = 1L;  // TODO: Double check what this does

    private static final int HEARTBEAT_CAPACITY = 2; // the number of logs per heartbeat

	private int term;  // The term of the sender of the heartbeat
    private int leaderCommitIndex; // the commit index the leader sends

    private int prevLogIndex; // the prevLogIndex to match with the Follower
    private Log prevLog; // the Term at prevLogIndex to match with the Follower

	private List<Log> entries;  // The Entries the leader wants to send to the Follower
    
	private Boolean reply; // What does the Follower say in response?
	private Route originRoute; // The origin of the Heartbeat (Leader's Route)
	private Route responderRoute; // The route of the Follower responding to the heartbeat


    /**
     * The Constructor for the Heartbeat
     *
     * @param hostInfo
     * @param updates
     * @param destination
     * @param rt
     * @param ledger
     */
	public HeartBeat(HostInfo hostInfo, List<Log> updates, Route destination, RoutingTable rt, Ledger ledger) {
		this.term = hostInfo.getTerm();
		this.entries = updates;
		this.reply = null; // allows us to determine whether this is appendEntries RPC or Response RPC
		this.originRoute = hostInfo.getRoute();
		this.responderRoute = destination;
        this.prevLogIndex = rt.getMatchIndex(destination); // prevLogIndex is equivalent to matchIndex - 1
        this.prevLog = ledger.getLogbyIndex(this.prevLogIndex-1); // null if prevLogIndex == -1
        this.leaderCommitIndex = ledger.getCommitIndex();
	}

	@Override
    public String toString(){
	    String result = "term: " + this.term;
	    result = result + " | prevLogIndex: " + this.prevLogIndex;
	    if(this.prevLog != null) {
            result = result + " | prevLog: " + this.prevLog.toString();
        }
        return result;
    }


    /**
     * Get the number of entries sent in each AppendEntries RPC
     * @return
     */
    public static int getHeartbeatCapacity() { return HEARTBEAT_CAPACITY; }

    /**
     * if hasReplied() == false: Get the Term of the Leader
     * else: Get the Term of the Follower
     * @return
     */
    public int getTerm() { return term; }

    /**
     * Get the commit Index of the Leader who sent the Heartbeat
     * @return
     */
    public int getLeaderCommitIndex() { return leaderCommitIndex; }

    /**
     * Set the Term of the Heartbeat
     * @param new_term
     */
    public void setTerm(int new_term) { this.term = new_term; }

    /**
     * Get the Entries stored in the Heartbeat
     * @return
     */
	public List<Log> getEntries() { return entries; }

    /**
     * Get the PrevLogIndex the Leader tries to match with the Follower
     * @return
     */
    public int getPrevLogIndex() { return prevLogIndex; }

    /**
     * Get the PrevLog at PrevLogIndex the Leader tries to match with the Follower
     * @return
     */
    public Log getPrevLog() { return prevLog; }

    /**
     * Test to see if this is an AppendEntries RPC (leader -> follower) or a response (follower -> leader)
     * @return
     */
    public boolean hasReplied() { return this.reply != null; }

    /**
     * Get the reply value (true or false) of the follower
     * @return
     */
	public Boolean getReply () { return this.reply; }

    /**
     * Set the reply value of the Heartbeat
     * @param response
     */
	public void setReply(boolean response) { this.reply = response; }

    /**
     * Get the origin of the Heartbeat (Leader's Route)
     * @return
     */
    public Route getOriginRoute() { return originRoute; }

    /**
     *
     * @param originRoute
     */
    public void setOriginRoute(Route originRoute) { this.originRoute = originRoute; }

    public Route getResponderRoute() { return responderRoute; }

    public void setResponderRoute(Route responderRoute) { this.responderRoute = responderRoute; }
}

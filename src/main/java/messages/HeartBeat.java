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
     * The Constructor for the Heartbeat (AppendEntries RPC)
     *
     * @param hostInfo The {@link HostInfo} Object to obtain relevant info for this heartbeat
     * @param updates the List of {@link Log} entries to be sent in this Heartbeat (AppendEntries RPC)
     * @param destination The {@link} Route which is the (intended) follower this Heartbeat is being sent to
     * @param rt The {@link RoutingTable} by which to obtain the prevLogIndex and prevLog info
     * @param ledger The {@link Ledger} object to obtain info needed in this heartbeat
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

    public HeartBeat(HostInfo hostInfo, List<Log> updates, Route destination, int prevLogIndex, Log prevLog, int leaderCommitIndex) {
        this.term = hostInfo.getTerm();
        this.entries = updates;
        this.reply = null; // allows us to determine whether this is appendEntries RPC or Response RPC
        this.originRoute = hostInfo.getRoute();
        this.responderRoute = destination;
        this.prevLogIndex = prevLogIndex; // prevLogIndex is equivalent to matchIndex - 1
        this.prevLog = prevLog; // null if prevLogIndex == -1
        this.leaderCommitIndex = leaderCommitIndex;
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
     *
     * @return retursn the number of entries to be sent in each Heartbeat
     */
    public static int getHeartbeatCapacity() { return HEARTBEAT_CAPACITY; }

    /**
     * if hasReplied() == false: Get the Term of the Leader
     * else: Get the Term of the Follower
     *
     * @return Returns the term of given heartbeat
     */
    public int getTerm() { return term; }

    /**
     * Get the commit Index of the Leader who sent the Heartbeat
     *
     * @return returns the leader commit index of the given heartbeat
     */
    public int getLeaderCommitIndex() { return leaderCommitIndex; }

    /**
     * Set the Term of the Heartbeat
     *
     * @param new_term sets the term of this heratbeat
     */
    public void setTerm(int new_term) { this.term = new_term; }

    /**
     * Get the Entries stored in the Heartbeat
     *
     * @return returns the List of {@link Log} updates in this Heartbeat
     */
    public List<Log> getEntries() { return entries; }

    /**
     * Get the PrevLogIndex the Leader tries to match with the Follower
     *
     * @return returns the prevLogIndex of this Heartbeat
     */
    public int getPrevLogIndex() { return prevLogIndex; }

    /**
     * Get the PrevLog at PrevLogIndex the Leader tries to match with the Follower
     *
     * @return returns the {@link Log} prevLog Object in this heartbeat
     */
    public Log getPrevLog() { return prevLog; }

    /**
     * Test to see if this is an AppendEntries RPC (leader to follower) or a response (follower to leader)
     *
     * @return returns a boolean to test whether this a initiating or response Heartbeat
     */
    public boolean hasReplied() { return this.reply != null; }

    /**
     * Get the reply value (true or false) of the follower
     *
     * @return returns Boolean to determine whether response is False or True
     */
    public Boolean getReply () { return this.reply; }

    /**
     * Set the reply value of the Heartbeat
     *
     * @param response the boolean value to set the response of this Hearbeat to
     */
    public void setReply(boolean response) { this.reply = response; }

    /**
     * Get the origin of the Heartbeat (Leader's Route)
     *
     * @return returns the origin {@link Route} of this Heartbeat (the initiating Leader)
     */
    public Route getOriginRoute() { return originRoute; }

    /**
     * Set the originating (Leader) Route of a Heartbeat
     *
     * @param originRoute The {@link Route} to set this Heartbeat to
     */
    public void setOriginRoute(Route originRoute) { this.originRoute = originRoute; }

    /**
     * Get the Route of the responder to the Heartbeat
     *
     * @return returns the  {@link Route}  of the responder to the Heartbeat
     */
    public Route getResponderRoute() { return responderRoute; }

}

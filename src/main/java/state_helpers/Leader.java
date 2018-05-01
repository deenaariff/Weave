package state_helpers;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import messages.HeartBeat;
import routing.Route;
import routing.RoutingTable;

import java.util.ArrayList;
import java.util.List;

public class Leader {

    /**
     * Determine the updates to send to a Follower based upon how consistent their logs are.
     * If no updates to send return an empty ArrayList<Log>
     *
     * @param route
     * @param rt
     * @param ledger
     * @return
     */
    public static List<Log> determineUpdates(Route route, RoutingTable rt, Ledger ledger) {
        boolean followerSynced = (rt.getMatchIndex(route) == ledger.getLastApplied());  // determine whether the follower is up to date with the leader's log entries
        int start_index = rt.getNextIndex(route); // the start of new logs to send to the server
        int num_logs = HeartBeat.getHeartbeatCapacity(); // the maximum number of logs to send in a heartbeat

        return (followerSynced)? new ArrayList<Log>() : ledger.getLogs(start_index,num_logs); // Set the number of updates to send to the follower
    }

    /**
     * Handle a HeartBeat Message that is received as a Follower. Multiple Cases
     * 1) From a Follower - Responded True
     * 2) From A Follower - Responded False
     * 3) From another Leader - Term Handling to resolve multiple leaders
     *
     * @param hb
     * @param ledger
     * @param host_info
     * @param rt
     */
    public static void HandleHeartBeat(HeartBeat hb, Ledger ledger, HostInfo host_info, RoutingTable rt) {
        if(hb.hasReplied() && host_info.matchRoute((hb.getRoute()))) {  // Heartbeat is acknowledged and is from me (From a Follower)
            if(hb.getReply()) {
                ledger.receiveConfirmation(hb,rt); // this should update the commitMap
            } else if(hb.getTerm() > host_info.getTerm()) {
                host_info.becomeFollower();
            }
        }
    }


}

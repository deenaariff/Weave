package state_helpers;

import Logger.Logger;
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
     * Determine the updates to send to a Follower based upon how consistent
     * their logs are.
     *
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
        Logger logger = new Logger(host_info);
        if (hb.hasReplied() && host_info.matchRoute((hb.getRoute()))) {  // Heartbeat is acknowledged and is from me (From a Follower)
            logger.log("Received Response From Follower " + hb.getRoute().getIP() + ":" + hb.getRoute().getHeartbeatPort());
            if (hb.getReply()) { // Checks if Follower Response is True
                logger.log("Follower Replied True!");
                ledger.receiveConfirmation(hb,rt); // this should update the commitMap
            } else if (hb.getTerm() > host_info.getTerm()) { // Checks if Follower Response is False
                logger.log("Follower Replied False!");
                host_info.becomeFollower();
            }
        } else if (!hb.hasReplied() && host_info.matchRoute((hb.getRoute()))) {  // Heartbeat is not acknowledged and is from me (From a Follower)
            // Follower is not synced (prevLog does not match)

            // rt.updateServerIndex(__, -1);  // Decrement the nextIndex value for this route

            // In the next heartbeat, we will check the value of the decremented index in the list of logs

            // Once we finally match values and indices, we should update the follower's nextIndex and matchIndex

            // Once those are updated, heartbeats will be acknowledged and updated properly

        } else if (hb.getTerm() > host_info.getTerm()) { // Received Response from another leader
            logger.log("Received Heartbeat From Another Follower with Greater Term");
            host_info.becomeFollower();
        }
    }
}

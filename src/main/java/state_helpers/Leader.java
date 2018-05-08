package state_helpers;

import info.HostInfo;
import ledger.Ledger;
import messages.HeartBeat;
import routing.RoutingTable;

public class Leader {

    /**
     * This method handles the leader's incoming heartbeat messages.
     *
     * If the heartbeat is an acknowledgement from a follower ...
     *
     * If the heartbeat is from another leader ...
     *
     * @param hb
     * @param ledger
     * @param host_info
     * @param rt
     */
    public static void HandleHeartBeat(HeartBeat hb, Ledger ledger, HostInfo host_info, RoutingTable rt) {

        if (hb.hasReplied() && host_info.matchRoute((hb.getRoute()))) {  // Heartbeat is acknowledged and is from me

            if(hb.getReply() == true) {
                // update nextIndex() and matchIndex();
                ledger.receiveConfirmation(hb,rt);  // this should update the commitMap
            } else {
                if(hb.getTerm() > host_info.getTerm()) {
                    host_info.becomeFollower();
                }
            }

        } else {  // Heartbeat is from another leader
            System.out.println("[Leader]: HEARTBEAT RECEIVED FROM ANOTHER LEADER. Check Term.");
        }
    }


}

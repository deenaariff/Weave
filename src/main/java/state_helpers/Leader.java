package state_helpers;

import info.HostInfo;
import ledger.Ledger;
import messages.HeartBeat;
import routing.RoutingTable;

public class Leader {

    public static void HandleHeartBeat(HeartBeat hb, Ledger ledger, HostInfo host_info, RoutingTable rt) {

        // Heartbeat is acknowledged and is from me
        if(hb.hasReplied() && host_info.matchRoute((hb.getRoute()))) {

            if(hb.getReply() == true) {
                // update nextIndex() and matchIndex();
                ledger.receiveConfirmation(hb,rt); // this should update the commitMap
            } else {
                if(hb.getTerm() > host_info.getTerm()) {
                    host_info.becomeFollower();
                }
            }

        }
    }


}

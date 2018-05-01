package state_helpers;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import messages.HeartBeat;
import routing.Route;
import rpc.rpc;

import java.io.IOException;

public class Follower {

    public static void HandleHeartBeat(HeartBeat hb, Ledger ledger, HostInfo host_info) throws IOException {
        // ensure this is a heartbeat from a leader, yet to be acknowledged
        if(!hb.hasReplied()) {

            // Check if equal or behind leader term
            if(host_info.getTerm() <= hb.getTerm()) {

                int prevIndex = hb.getPrevLogIndex();
                Log prevLogTerm = hb.getPrevLog();

                // Ensure prevLog Term matches at given index
                if(ledger.confirmMatch(prevIndex, prevLogTerm) == true) {
                    ledger.update(hb);
                    hb.setReply(true);
                } else {
                    hb.setReply(false);
                }

                // False if term is ahead of leader term
            } else {
                hb.setReply(false);
            }

            Route origin = hb.getRoute();

            // update the origin info for the heartbeat on response
            hb.setTerm(host_info.getTerm());
            hb.setRoute(host_info.getRoute());

            // if replied true, ensure my commitIndex is synced
            if(hb.getReply() == true) {
                ledger.syncCommitIndex(hb.getLeaderCommitIndex());
            }

            // return heartbeat to the destination
            rpc.returnHeartbeat(hb, origin);
        }
    }
}

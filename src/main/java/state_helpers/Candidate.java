package state_helpers;

import info.HostInfo;
import messages.HeartBeat;
import routing.Route;
import rpc.rpc;

import java.io.IOException;

public class Candidate {

    public static void HandleHeartBeat(HeartBeat hb, HostInfo host_info) throws IOException {
        if (hb.getTerm() >= host_info.getTerm()) {
            host_info.setVote(hb.getRoute());
            host_info.becomeFollower();
        }  else {
            hb.setReply(false);

            Route origin = hb.getRoute();

            // update the origin info for the heartbeat on response
            hb.setTerm(host_info.getTerm());
            hb.setRoute(host_info.getRoute());

            // return heartbeat to the destination
            rpc.returnHeartbeat(hb, origin);
        }
    }

}

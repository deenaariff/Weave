package state_helpers;

import info.HostInfo;
import messages.Vote;
import voting_booth.VotingBooth;
import messages.HeartBeat;
import routing.Route;
import rpc.rpc;
import Logger.Logger;

import java.io.IOException;

public class Candidate {

    /**
     * This method handles heartbeats received from a leader while the node is
     * currently in a candidate state.
     *
     * If the heartbeat received is from a legitimate new leader, reset and
     * become a follower.
     *
     * Else, respond to the heartbeat without the acknowledgement.
     *
     * @param hb
     * @param host_info
     * @throws IOException
     */
    public static void HandleHeartBeat(HeartBeat hb, HostInfo host_info) throws IOException {
        if (hb.getTerm() >= host_info.getTerm()) {
            System.out.println("Leader with Term >= current Term Detected. Becoming Follower");
            host_info.setVote(hb.getRoute());
            host_info.becomeFollower();
        } else {
            hb.setReply(false);

            Route origin = hb.getRoute();

            // update the origin info for the heartbeat on response
            hb.setTerm(host_info.getTerm());
            hb.setRoute(host_info.getRoute());

            // return heartbeat to the destination
            rpc.returnHeartbeat(hb, origin);
        }
    }

    /**
     * This method handles a vote if the node is currently in a candidate state.
     *
     * If the votes are tagged with the route of the current node, it will check
     * to see if it was cast a vote and continue counting votes until a timeout.
     *
     * If the votes are tagged with the route of a different node ...
     *
     * @param vote rpc object containing voting info
     * @param vb voting booth which maintains election functionality
     * @param host_info info of the current host
     */
    public static void HandleVote(Vote vote, VotingBooth vb, HostInfo host_info) {
        Logger logger = new Logger(host_info);

        if (host_info.matchRoute(vote.getRoute())) {  // Received response to our RequestVote RPC

            if (vote.getVoteStatus()) {  // Somebody voted for us!
                vb.incrementVotes();
            }

            if (vb.checkIfWonElection()) {  // Have we received majority votes yet?
                logger.log("[" + host_info.getState() + "]: Election Won, becoming Leader");
                host_info.becomeLeader();
            }

            if (vb.isElectionOver()) {  // Check election timeout
                logger.log("[" + host_info.getState() + "]: Election Lost, restarting election");
                vb.startElection();
            }

        } else {  // Other nodes are requesting a vote

            if(true) {
                // TODO: move returnVote to non-state_helpers dependent class

                try {
                    rpc.returnVote(vote);
                } catch (java.io.IOException e) {
                    System.out.println(e);
                }

            } else {
                // handle term based cased
            }
        }
    }


}

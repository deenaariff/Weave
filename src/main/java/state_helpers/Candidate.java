package state_helpers;

import info.HostInfo;
import messages.Vote;
import rpc.rpc;
import voting_booth.VotingBooth;

public class Candidate {


    public static void HandleHeartBeat() {

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

        if (host_info.matchRoute(vote.getRoute())) {  // Received response to our RequestVote RPC

            if (vote.getVoteStatus()) {  // Somebody voted for us!
                vb.incrementVotes();
            }

            if (vb.checkIfWonElection()) {  // Have we received majority votes yet?
                System.out.println("[" + host_info.getState() + "]: Election Won, becoming Leader");
                host_info.becomeLeader();
            }

            if (vb.isElectionOver()) {  // Check election timeout
                System.out.println("[" + host_info.getState() + "]: Election Lost, restarting election");
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

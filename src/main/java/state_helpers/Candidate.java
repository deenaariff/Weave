package state_helpers;

import info.HostInfo;
import ledger.Ledger;
import messages.Vote;
import routing.RoutingTable;
import voting_booth.VotingBooth;
import messages.HeartBeat;
import routing.Route;
import rpc.rpc;

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
            host_info.setVote(hb.getOriginRoute()); // Update who I voted for
            host_info.becomeFollower(); // A follower has sent an Append Entries RPC, become a follower
        } else {
            hb.setReply(false);
            Route origin = hb.getOriginRoute();

            hb.setTerm(host_info.getTerm()); // update the term so leader can step down if necessary
            hb.setOriginRoute(host_info.getRoute());

            rpc.returnHeartbeat(hb, origin); // return heartbeat to the destination
        }
    }

    /**
     * This method handles a vote if the node is currently in a candidate state.
     *
     * If the votes are tagged with the route of the current node, it will check
     * to see if it was cast a vote and continue counting votes until a timeout.
     *
     * If the vote requests are tagged with the route of a different node we check if
     * the term is greater than us. If so, we vote for that candidate.
     *
     * @param vote rpc object containing voting info
     * @param vb voting booth which maintains election functionality
     * @param host_info info of the current host
     */
    public static void HandleVote(Vote vote, VotingBooth vb, HostInfo host_info, RoutingTable rt, Ledger ledger) {

        if (host_info.matchRoute(vote.getRoute()) && vote.getVoteStatus()) {  // Received response to our RequestVote RPC
            vb.incrementVotes(vote.getResponder());

            if (vb.checkIfWonElection()) {  // Have we received majority votes yet?
                vb.printWon();
                host_info.becomeLeader(); // we now become a leader node
            }
        } else if (host_info.matchRoute(vote.getRoute()) == false && vote.getVoteStatus() == false) {  // Other candidates are requesting a vote
            try {
                boolean greater_term = (vote.getTerm() > host_info.getTerm());
                //boolean up_to_date = ledger.validateVote(vote);

                if (greater_term) { // if the requesting candidate has a greater term and is up_to_date, cast vote
                    vote.castVote(host_info.getId());
                    host_info.setVoteFlag(true);
                    host_info.setVote(vote.getRoute());
                }
                rpc.returnVote(vote); // return vote to the requesting candidate
            } catch (java.io.IOException e) {
                System.out.println(e);
            }
        }
    }


}

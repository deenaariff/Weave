package rpc_vote;

import ledger.Ledger;
import state_helpers.Candidate;
import state_helpers.Follower;
import Logger.Logger;
import voting_booth.VotingBooth;
import info.HostInfo;
import messages.Vote;
import routing.RoutingTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.*;

/**
 * This is a Runnable that implements a socket to listen for incoming messages on
 * the Voting Port. Based on the state of the RAFT node it will call a handler
 * function to respond to a Vote Request in an appropriate manner as traditionally
 * done in RAFT.
 *
 */
public class VotingListener implements Runnable {

    private ServerSocket listener;
    private HostInfo host_info;
    private RoutingTable rt;
    private VotingBooth vb;
    private Ledger ledger;
    private Logger logger;

    /**
     * Constructor for the Voting Listener
     *
     * @param host_info
     * @param rt
     * @param vb
     * @param ledger
     * @param logger
     */
    public VotingListener(HostInfo host_info, RoutingTable rt, VotingBooth vb, Ledger ledger, Logger logger) {
        this.host_info = host_info;
        this.rt = rt;
        this.vb = vb;
        this.logger = logger;
        this.ledger = ledger;
    }

    @Override
    public void run() {

        try {
            this.listener = new ServerSocket(this.host_info.getVotingPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) { // Listen for the heartbeat until the waiting time interval has elapsed

            // Check for split vote at end of election, and restart election
            if (this.host_info.isCandidate() && this.vb.checkIfWonElection() == false && this.vb.isElectionOver()) {
                this.vb.printLost();
                this.host_info.becomeCandidate(this.vb, this.rt, this.ledger);
            }

            try {
                Socket socket = listener.accept(); // accept incoming mesages

                final InputStream yourInputStream = socket.getInputStream();
                final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                final Vote vote = (Vote) inputStream.readObject();

                if (vote.getVoteStatus()) { // Is this vote acknowledged (voter -> requester)
                    logger.log("Received Acknowledged Vote w/ Origin : " + vote.getHostName() + ":" + vote.getEndpointPort());
                } else { // Is this vote a requested vote (requester -> voter)
                    logger.log("Received Request Vote w/ Origin : " + vote.getHostName() + ":" + vote.getEndpointPort());
                }

                // State machine handle of the vote object
                if (this.host_info.isLeader()) {
                    // term based handling
                } else if (this.host_info.isCandidate()) {
                    Candidate.HandleVote(vote, this.vb, this.host_info, this.rt);
                } else if (this.host_info.isFollower()) {
                    Follower.HandleVote(vote, this.vb, this.host_info, this.ledger);
                }

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}

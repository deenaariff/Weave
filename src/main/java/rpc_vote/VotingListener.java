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

public class VotingListener implements Runnable {

    private ServerSocket listener;
    private HostInfo host_info;
    private RoutingTable rt;
    private VotingBooth vb;
    private Ledger ledger;
    private Logger logger;

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

        // Listen for the heartbeat until the waiting time interval has elapsed
        while (true) {

            try {

                Socket socket = listener.accept();

                final InputStream yourInputStream = socket.getInputStream();
                final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                final Vote vote = (Vote) inputStream.readObject();

                if (vote.getVoteStatus()) {
                    logger.log("Received Acknowledged Vote w/ Origin : " + vote.getHostName() + ":" + vote.getEndpointPort());
                } else {
                    logger.log("Received Request Vote w/ Origin : " + vote.getHostName() + ":" + vote.getEndpointPort());
                }

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

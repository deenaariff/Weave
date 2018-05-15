package rpc_vote;

import state_helpers.Candidate;
import state_helpers.Follower;
import Logger.Logger;
import voting_booth.VotingBooth;
import rpc.rpc;
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
    private Logger logger;

    public VotingListener(HostInfo host_info, RoutingTable rt, VotingBooth vb, Logger logger) {
        this.host_info = host_info;
        this.rt = rt;
        this.vb = vb;
        this.logger = logger;
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
                    Candidate.HandleVote(vote, vb, host_info);
                } else if (this.host_info.isFollower()) {
                    Follower.HandleVote(vote, vb, host_info);
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

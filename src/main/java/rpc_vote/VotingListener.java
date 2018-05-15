package rpc_vote;

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
            listener.setSoTimeout(HostInfo.getElectionInterval());
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

                if(vote.getVoteStatus()) {
                    logger.log("Received Acknowledged Vote w/ Origin : " + vote.getHostName() + ":" + vote.getEndpointPort());
                } else {
                    logger.log("Received Request Vote w/ Origin : " + vote.getHostName() + ":" + vote.getEndpointPort());
                }

                if(this.host_info.isLeader()) {

                    // term based handling

                } else if(this.host_info.isCandidate()) {

                    if(this.host_info.matchRoute(vote.getRoute())) {

                        boolean voteStatus = vote.getVoteStatus();

                        // If voteStatus indicates the node has voted for you, the updated number of votesObtained

                        if(voteStatus == true) {
                            vb.incrementVotes();
                        }

                    } else {
                        logger.log("Returning vote - " + vote.getHostName() + ":" + vote.getVotingPort());
                        rpc.sendVote(vote,vote.getHostName(),vote.getVotingPort());
                    }

                    if(vb.isElectionOver()) {
                        int state_change = vb.endElection(host_info);
                        if(state_change == 1) {
                            logger.log("Election Won, becoming Leader");
                            host_info.becomeLeader();
                        } else if(state_change == 0) {
                            logger.log("Election Lost");
                            host_info.becomeFollower();
                        }
                    }

                } else if(this.host_info.isFollower()) {

                    // Candidate requesting Vote should have higher term than rpc
                    if(this.host_info.getTerm() > vote.getTerm()) {
                        if (this.host_info.hasVoted() == false) {
                            vote.castVote();
                            this.host_info.setVote(vote.getRoute());
                        }
                    }
                    logger.log("Returning vote - " + vote.getHostName() + ":" + vote.getVotingPort());
                    rpc.sendVote(vote,vote.getHostName(),vote.getVotingPort());
                }

                socket.close();

            } catch (SocketTimeoutException s) {
                if(this.host_info.isCandidate() && vb.isElectionOver()) {
                    int state_change = vb.endElection(host_info);
                    if(state_change == 1) {
                        logger.log("Election Won, becoming Leader");
                        host_info.becomeLeader();
                    } else if(state_change == 0) {
                        logger.log("Election Lost");
                        host_info.becomeFollower();
                    }
                }
            } catch(ConnectException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        /*try {
            this.listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }


}

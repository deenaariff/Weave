package rpc_vote;

import VotingBooth.VotingBooth;
import rpc.rpc;
import info.HostInfo;
import messages.Vote;
import routing.RoutingTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class VotingListener implements Runnable {

    private ServerSocket listener;
    private HostInfo host_info;
    private RoutingTable rt;
    private VotingBooth vb;


    public VotingListener(HostInfo host_info, RoutingTable rt, VotingBooth vb) {
        this.host_info = host_info;
        this.rt = rt;
        this.vb = vb;
    }


    @Override
    public void run() {

        int totalTableLength = rt.getTable().size();

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
                System.out.println("[" + this.host_info.getState() + "]: Received a Vote Object");

                final InputStream yourInputStream = socket.getInputStream();
                final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                final Vote vote = (Vote) inputStream.readObject();

                if(this.host_info.isLeader()) {

                    // term based handling

                } else if(this.host_info.isCandidate()) {

                    if(this.host_info.matchRoute(vote.getRoute())) {

                        boolean voteStatus = vote.getVoteStatus();

                        // If voteStatus indicates the node has voted for you, the updated number of votesObtained

                        if(voteStatus == true) {
                            vb.incrementVotes();
                        }

                        if(vb.isElectionOver()) {
                            int state_change = vb.endElection(host_info);
                            if(state_change == 1) {
                                host_info.becomeLeader();
                            } else if(state_change == 0) {
                                host_info.becomeFollower();
                            }
                        }

                    } else {

                        if(true) {
                            // TODO: move returnVote to non-state dependent class
                            rpc.returnVote(vote);
                        } else {
                            // handle term based cased
                        }
                    }

                } else if(this.host_info.isFollower()) {

                    // Candidate requesting Vote should have higher term than rpc
                    if(true) {
                        if (this.host_info.getVote() == false) {
                            vote.castVote();
                            this.host_info.setVote(true);
                        }
                        rpc.returnVote(vote);
                    } else {
                        // handle term based cased
                    }
                }

                socket.close();

            } catch (SocketTimeoutException s) {
                if(this.host_info.isCandidate() && vb.isElectionOver()) {
                    int state_change = vb.endElection(host_info);
                    if(state_change == 1) {
                        System.out.println("[" + this.host_info.getState() + "]: Election Won, becoming Leader");
                        host_info.becomeLeader();
                    } else if(state_change == 0) {
                        System.out.println("[" + this.host_info.getState() + "]: Election Lost, becoming Follower");
                        host_info.becomeFollower();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }

        try {
            this.listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

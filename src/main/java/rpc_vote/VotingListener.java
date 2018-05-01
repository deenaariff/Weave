package rpc_vote;

import state_helpers.Candidate;
import voting_booth.VotingBooth;
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

        try {
            this.listener = new ServerSocket(this.host_info.getVotingPort());
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

                if (this.host_info.isLeader()) {

                    // term based handling

                } else if (this.host_info.isCandidate()) {
                    Candidate.HandleVote(vote, vb, host_info);
                } else if (this.host_info.isFollower()) {

                    // Candidate requesting Vote should have higher term than rpc
                    if (true) {
                        if (this.host_info.hasVoted() == false) {
                            vote.castVote();
                            this.host_info.setVote(vote.getRoute());
                        }
                        rpc.returnVote(vote);
                    } else {
                        // handle term based cased
                    }
                }

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
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

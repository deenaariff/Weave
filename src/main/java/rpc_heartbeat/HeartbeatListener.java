package rpc_heartbeat;

import logger.Logger;
import info.HostInfo;
import ledger.Ledger;
import messages.HeartBeat;
import routing.RoutingTable;
import state_helpers.Candidate;
import state_helpers.Follower;
import state_helpers.Leader;
import voting_booth.VotingBooth;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HeartbeatListener implements Runnable {

    private HostInfo host_info;
    private Ledger ledger;
    private RoutingTable rt;
    private ServerSocket listener;
    private VotingBooth vb;
    private Logger logger;

    /**
     * Constructor for HeartbeatListener
     *
     * @param host_info The {@link HostInfo} of the ndoe
     * @param ledger The {@link Ledger} of the Node
     * @param rt The {@link RoutingTable} of the Node
     * @param vb The {@link VotingBooth} of the node
     */
    public HeartbeatListener(HostInfo host_info, Ledger ledger, RoutingTable rt, VotingBooth vb) {
        this.host_info = host_info;
        this.ledger = ledger;
        this.rt = rt;
        this.vb = vb;
        this.logger = host_info.getLogger();
    }

    @Override
    public void run() {

        try {
            this.listener = new ServerSocket(this.host_info.getHeartBeatPort());
            listener.setSoTimeout(this.host_info.getHeartbeatTimeoutInterval());
            logger.log("Heartbeat Timeout Interval (" + this.host_info.getHeartbeatTimeoutInterval() + "ms )");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) { // Listen for the heartbeat until the waiting time interval has elapsed
            try {

                Socket socket = listener.accept();

                final InputStream yourInputStream = socket.getInputStream();
                final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
                final HeartBeat hb = (HeartBeat) inputStream.readObject();

                if(this.host_info.isLeader()) { // Handle HB in leader state
                    Leader.HandleHeartBeat(hb, this.ledger, this.host_info, this.rt);
                } else if(this.host_info.isCandidate()) { // Handle HB in Candidate state
                    Candidate.HandleHeartBeat(hb, this.host_info);
                } else if(this.host_info.isFollower()) { // Handle HB in Follower State
                    Follower.HandleHeartBeat(hb, this.ledger, this.host_info);
                }

                socket.close();

            } catch (SocketTimeoutException s) {
                if(this.host_info.isFollower() && this.host_info.voteFlag() == false) {
                    logger.log("Interval for Heart Beat Listener Elapsed : (" + this.host_info.getHeartbeatTimeoutInterval() + ")");
                    host_info.becomeCandidate(this.vb, this.rt, this.ledger);
                } else {
                    this.host_info.setVoteFlag(false);
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

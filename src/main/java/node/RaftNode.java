package node;
import VotingBooth.VotingBooth;
import ledger.Log;
import rpc.rpc;
import info.HostInfo;
import ledger.Ledger;
import routing.Route;
import routing.RoutingTable;
import rpc_heartbeat.HeartbeatListener;
import rpc_vote.VotingListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is the class that contains the implementation of the Raft Node and
 * for switching between states. This class passes essential information to 
 * each class that implements various state's logic.
 * 
 * @author deenaariff
 */
public class RaftNode {
	
	private Ledger ledger;
	private HostInfo host;
	private RoutingTable rt;
	private VotingBooth vb;
	
	/**
	 * The constructor for the RaftNode Class
	 * 
	 */
	public RaftNode(Ledger ledger, Route route) {
        this.rt = new RoutingTable();
        this.vb = new VotingBooth(this.rt,HostInfo.getElectionInterval());
		this.ledger = ledger;
		this.host = new HostInfo(route,this.vb);
	}

	/**
	 * Second constructor for the RaftNode Class.vb
     * Pass option Routing Table
	 *
	 */
	public RaftNode(RoutingTable rt,  Ledger ledger, Route route) {
        this.rt = rt;
        this.vb = new VotingBooth(this.rt,HostInfo.getElectionInterval());
        this.ledger = ledger;
        this.host = new HostInfo(route, this.vb);
    }

	public void run() {

        Thread hb_thread = new Thread(new HeartbeatListener(this.host,this.ledger,this.rt));
        Thread voting_thread = new Thread(new VotingListener(this.host,this.rt,this.vb));

        hb_thread.start();
        voting_thread.start();

        try{
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(true) {
            //System.out.println(this.host.getState());
            if(this.host.isLeader()) {
                System.out.println("[" + this.host.getState() + "]: Broadcasting Messages to Followers");
                try{
                    TimeUnit.MILLISECONDS.sleep(this.host.getHeartbeatInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                List<Log> updates = ledger.getUpdates();
                rpc.broadcastHeartbeats(this.rt,updates,this.host);
            } else if(this.host.isCandidate()) {
                if(!host.isInitialized()) {
                    System.out.println("[" + this.host.getState() + "]: Requesting Votes from Followers");
                    rpc.broadcastVotes(this.rt,this.host);
                    host.hasBeenInitialized();
                }
            } else if(this.host.isFollower()) {
                //System.out.println("[" + this.host.getState() + "]: Break 1");
            }

        }

    }

	/***
	 * Returns the Node's Ledger
	 * 
	 * @return
	 */
	public Ledger getLedger() {
		return ledger;
	}
	
	/**
	 * Return's the nodes current state
	 * 
	 * @return
	 */
	public HostInfo getHostInfo() {
		return host;
	}
	
}

package node;
import rpc.rpc;
import info.HostInfo;
import ledger.Ledger;
import routing.RoutingTable;
import java.util.concurrent.TimeUnit;

/**
 * This is the class that contains the implementation of the Weave Node and
 * for switching between states. This class passes essential information to 
 * each class that implements various state_helpers's logic.
 * 
 * @author deenaariff
 */
public class RaftNode implements Runnable {
	
	private Ledger ledger;
	private HostInfo host;
	private RoutingTable rt;

	/**
	 * Constructor for the RaftNode Class.vb
     * Pass option Routing Table
	 *
	 */
	public RaftNode(RoutingTable rt, Ledger ledger, HostInfo host) {
        this.rt = rt;
        this.host = host;
        this.ledger = ledger;
    }

	public void run() {

        try{
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(true) {
            //System.out.println(this.host.getState());
            if(this.host.isLeader()) {
                System.out.println("[" + this.host.getState() + "]: Last Index Committed: " + this.ledger.getCommitIndex());
                System.out.println("[" + this.host.getState() + "]: Logs in Ledger: " + this.ledger.getLastApplied());
                System.out.println("[" + this.host.getState() + "]: Broadcasting Messages to Followers");
                try{
                    TimeUnit.MILLISECONDS.sleep(this.host.getHeartbeatInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                rpc.broadcastHeartbeatUpdates(this.rt,this.ledger,this.host);
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
	 * Return's the nodes current state_helpers
	 * 
	 * @return
	 */
	public HostInfo getHostInfo() {
		return host;
	}
	
}

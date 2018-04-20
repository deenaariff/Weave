package rpc_heartbeat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

import info.HostInfo;
import ledger.Ledger;
import messages.HeartBeat;
import routing.RoutingTable;

/**
 * This class will be instantiated by a follower, and it will listen for
 * incoming heartbeat messages.
 *
 * After receiving the heartbeats, this class will de-serialize the object
 * and update its ledger to align with the leader's ledger.
 */
public class FollowerListenHeartBeat implements Callable<Void> {

	private Ledger ledger; // ledger to append new heart beats to
    private HostInfo host_info;
	private int random_interval;
    private Long last_heartbeat; // time stamp of the last heart beat received

    /**
     * Constructor for the FollowerListenHeartBeat class
     *
     * @param ledger
     * @param host_info
     * @param random_interval
     */
	public FollowerListenHeartBeat(Ledger ledger, HostInfo host_info, int random_interval) {
		this.ledger = ledger;
		this.host_info = host_info;
		this.random_interval = random_interval;
	}


    /**
     * This callable listens for the heartbeat messages sent from the leader.
     *
     * If a heartbeat is received, we de-serialize the heartbeat and update the
     * ledger. If a heartbeat is not received within the random time interval,
     * we terminate the thread and become a candidate.
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
	public Void call() throws IOException, ClassNotFoundException {
	    System.out.println("[Follower]: Listening for Incoming Heartbeat Messages");
		ServerSocket listener = new ServerSocket(this.host_info.getHeartBeatPort());
		this.last_heartbeat = System.nanoTime();

	    try {
	        // Listen for the heartbeat until the waiting time interval has elapsed
	    	while (true) {
	    	    long elapsed = (System.nanoTime() - this.last_heartbeat) / 1000000;
	    		System.out.println("Time Elapsed: " + elapsed);
	    		if(elapsed > this.random_interval) {
	    			System.out.println("[Follower]: Randomized Follower Waiting Interval (" + this.random_interval +  "  ms) Elapsed - Revert to Candidate");
	    			break;
	    		} else {
		    		Socket socket = listener.accept();  // TODO: Will this block?? We still need to check interval
		            try {
		            	System.out.println("Received a Heartbeat");

		            	// De-serialize the heartbeat object received
		            	final InputStream yourInputStream = socket.getInputStream();
		                final ObjectInputStream inputStream = new ObjectInputStream(yourInputStream);
		                final HeartBeat hb = (HeartBeat) inputStream.readObject();

                        // Record the time this heartbeat was received
                        this.last_heartbeat = System.nanoTime();

		                // Update the ledger based on the heartbeat received
                        ledger.update(hb);
						// TODO: [Follower]: Received a new log:  ___

                        // Send this heartbeat back to the leader to acknowledge
                        final OutputStream outputStream = socket.getOutputStream();
                        final ObjectOutputStream output = new ObjectOutputStream(outputStream);
                        output.writeObject(hb);
		            } finally {
		                socket.close();
		            }
	    		}
	        }
	    } finally {
            listener.close();
        }

	    return null;
	}
}

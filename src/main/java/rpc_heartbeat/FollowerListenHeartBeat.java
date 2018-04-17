package rpc_heartbeat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

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
    private RoutingTable rt;
    private int port;
	private int random_interval;
    private Long last_heartbeat; // time stamp of the last heart beat received

    /**
     * Constructor for the FollowerListenHeartBeat class
     *
     * @param ledger
     * @param rt
     * @param port
     * @param random_interval
     */
	public FollowerListenHeartBeat(Ledger ledger, RoutingTable rt, int port, int random_interval) {
		this.ledger = ledger;
		this.rt = rt;
		this.port = port;
		this.random_interval = random_interval;
        this.last_heartbeat = System.nanoTime();  // TODO: Is this ok? Can we say that the last heartbeat was the time of instantiation?
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
		ServerSocket listener = new ServerSocket(this.port);
		this.last_heartbeat = System.nanoTime();  // TODO: Check this with the message up there

	    try {
	        // Listen for the heartbeat until the waiting time interval has elapsed
	    	while (true) {
	    		if (this.last_heartbeat - System.nanoTime() > this.random_interval) {
	    			System.out.println("Randomized Follower Waiting Interval Elapsed");
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

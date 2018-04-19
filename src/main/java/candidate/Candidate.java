package candidate;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import info.HostInfo;
import ledger.Ledger;
import routing.RoutingTable;
import rpc_vote.RequestVote;
import state.State;

/**
 * This implements the logic for the Candidate state of a node
 * 
 */
public class Candidate extends State {
	
	private RoutingTable rt;
	private HostInfo host;
	private ExecutorService exec;

	public Candidate(Ledger ledger, RoutingTable rt, HostInfo host) {
		this.ledger = ledger;
		this.rt = rt;
		this.host = host;
		this.exec = Executors.newFixedThreadPool(3);
	}
	
	public Candidate(Ledger ledger, RoutingTable rt, HostInfo host, ExecutorService exec) {
		this.ledger = ledger;
		this.rt = rt;
		this.host = host;
		this.exec = exec;
	}

	@Override
	public int run() {
		
		// start an election
		Callable<Integer> requester = new RequestVote(this.rt, this.host);

		Future<Integer> future = exec.submit(requester);

		try {
			return future.get();
		} catch (Exception e) {
			// The exception will be printed out
			System.out.println("Exception: " + e);
		}

		return 0;
	}

}

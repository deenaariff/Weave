package ledger;

import messages.HeartBeat;
import org.springframework.stereotype.Component;
import routing.RoutingTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Ledger class is shared by all States and used to keep track
 * of all key-value entries that a node contains. It includes methods
 * to determine if a Ledger is out of sync with the cluster.
 * 
 * @author deenaariff
 *
 */
@Component
public class Ledger {

	// Necessary to all states
	private Map<String,String> keyStore; // Map all keys to values
	private List<Log> logs; // Store A Growing List of Log Values, equivalent to log[] in RAFT paper

	// Volatile to all states
	private int commitIndex;
	private int lastApplied;
	private HashMap<HeartBeat,Integer> commitMap;

	// Volatile for leader
	private List<Log> updateQueue; // a Queue storing all new Logs entries between Heartbeat broadcasts

	/**
	 * The Constructor for the Ledger Class
	 * 
	 */
	public Ledger() {
		this.keyStore = new HashMap<String,String>();
		this.logs = new ArrayList<Log>();
		this.updateQueue = new ArrayList<Log>();
		this.commitMap = new HashMap<HeartBeat,Integer>();
	}

	/**
	 * Update the HashMap Data structure representing the Key-Value store
	 * @param key The lookup key of the data to be entered.
	 * @param value The value mapped to the lookup key of the data being entered.
	 */
	private void updateKeyStore(String key, String value) {
		this.keyStore.put(key, value);
	}


	/**
	 *
	 * @param key
	 * @return
	 */
	public String getData(String key) {
		if(keyStore.containsKey(key)) {
			return keyStore.get(key);
		} else {
			throw new RuntimeException("This Key Has Not Been Commited to the Cluster");
		}
	}

	/**
	 * This method is called by client to add new log to the list of logs.
	 * This will be replicated to followers.
	 * @param value
	 */
	public void addToLog (Log value) {
		this.logs.add(value);
		this.lastApplied = this.logs.size() - 1;
	}

	/**
	 * Receive a Heartbeat Message from a Follower and update Ledger accordingly
	 * @param hb
	 * @param rt
	 */
	public void receiveConfirmation(HeartBeat hb, RoutingTable rt) {
		if (commitMap.containsKey(hb)) {
			Integer value = commitMap.get(hb);
			if (value == 1) {  // The last heartbeat to fulfill majority
				commitMap.remove(hb);
				commitToLogs(hb);
			} else {
				commitMap.put(hb, value-1);
			}
		} else {
			commitMap.put(hb, rt.getMajority());
		}
	}

	/**
	 * A method to return all new logs entries that have been queued in updateQueue List.
	 * Clears all entries from the updateQueue List.
	 * @return All logs that are stored in the member updateQueue Object.
	 */
	public List<Log> getUpdates() {
		List<Log> updates = new ArrayList<Log>();
		for(Log log : this.updateQueue) {
			updates.add(log);
		}
		this.updateQueue.clear();  // clear entries
		return updates;
	}


	/**
	 * Confirms whether a Log at a given index matches the equivalent Log in the ledger
	 * @param index
	 * @param term
	 * @return
	 */
	public Boolean confirmMatch(int index, Log term) {
		return logs.get(index).equals(term);
	}

	/**
	 * Given the commit Index from a Leader, update the commit index to be the min
	 * of the last applied log to our ledger and the leader's commit index.
	 * @param leader_commit_index
	 */
	public void syncCommitIndex(int leader_commit_index) {
		if(leader_commit_index > this.commitIndex) {
			this.commitIndex = (int) Math.min(leader_commit_index,this.lastApplied);
		}
	}

	/**
	 * This method is used by followers, and updates the ledger based on the
	 * heartbeat. It iterates through all of the new commits sent from the
	 * leader, and adds it to the logs and key store
	 *
	 * @param hb The heartbeat message sent from the leader
	 */
	public void update(HeartBeat hb) {
		for(Log log : hb.getEntries()) {
			this.logs.add(log);
			updateKeyStore(log.getKey(), log.getValue());
		}
	}

	/**
	 * This method is called once a majority of heartbeats have been received
	 * by the leader. This method commits all logs that are stored inside the heartbeat.
	 * @param hb
	 */
	public void commitToLogs(HeartBeat hb) {
		for (Log log : hb.getEntries()) {
			logs.add(log);
			updateKeyStore(log.getKey(),log.getValue());
		}
	}

	/**
	 * This members prints all current logs that have been stored in the ledger.
	 */
	public void printLogs() {
		System.out.println("Logs:");
		for(Log entry : logs) {
			System.out.println("  " + entry.getIndex() + " | term: " + entry.getTerm());
		}
		System.out.println("\nKey-Value Pairs:");
		for (String key : keyStore.keySet()) {
			System.out.println("  " + key + " - " + keyStore.get(key));
		}
	}

	public int getCommitIndex() { return commitIndex; }

	public int getLastApplied() { return lastApplied; }

	public void setLastApplied(int lastApplied) { this.lastApplied = lastApplied; }

    public Log getLogbyIndex(int index) { return logs.get(index); }


	/**
	 * Tests for the Ledger class.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
//		Ledger ledger = new Ledger();
//		ledger.commitToLogs(new Log(1,2,"password","goats"));
//		ledger.commitToLogs(new Log(2,3,"fire","hot"));
//		ledger.printLogs();
	}
	
}

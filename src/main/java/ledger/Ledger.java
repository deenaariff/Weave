package ledger;

import messages.HeartBeat;
import routing.Route;
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
public class Ledger {

	/** Necessary to all states **/
	private Map<String,String> keyStore; // Map all keys to values
	private List<Log> logs; // Store A Growing List of Log Values, equivalent to log[] in RAFT paper
	private List<Integer> appendMatch; // Keep track how many servers have appended for a given index

	/** Volatile to all states **/
	private int commitIndex;
	private int lastApplied;
	private HashMap<HeartBeat,Integer> commitMap;

	/** Volatile for leader **/
	private List<Log> updateQueue; // a Queue storing all new Logs entries between Heartbeat broadcasts

	/**
	 * The Constructor for the Ledger Class
	 */
	public Ledger() {
		this.keyStore = new HashMap<String,String>();
		this.logs = new ArrayList<Log>();
		this.appendMatch = new ArrayList<Integer>();
		this.updateQueue = new ArrayList<Log>();
		this.commitMap = new HashMap<HeartBeat,Integer>();
		this.commitIndex = 0;
		this.lastApplied = 0;
	}

	/**
	 * Update the HashMap Data structure representing the Key-Value store
	 *
	 * @param key The lookup key of the data to be entered.
	 * @param value The value mapped to the lookup key of the data being entered.
	 */
	private void updateKeyStore(String key, String value) {
		this.keyStore.put(key, value);
	}

    /**
     * Get the Key Store as HashMaps
     *
     * @return
     */
	public Map<String,String> getKeyStore() {
	    return this.keyStore;
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
	 * Get the Logs in that are in the interval of a given start index, to the # num_logs after that
	 *
	 * @param start_index
	 * @param num_logs
	 * @return
	 */
	public List<Log> getLogs(int start_index, int num_logs) {
		List<Log> updates = new ArrayList<Log>();
		for(int i = 0; i < num_logs; i++) {
			if(start_index + i < this.logs.size() - 1) {
				updates.add(this.logs.get(start_index+i));
			}
		}
		return updates;
	}

	/**
	 * This method is called by client to add new log to the list of logs.
	 * This will be replicated to followers.
	 *
	 * @param value
	 */
	public void addToLogs(Log value) {
		this.logs.add(value);
		this.appendMatch.add(0);
		this.lastApplied = this.logs.size();
	}

	/**
	 * Receive a Heartbeat Message from a Follower and update Ledger accordingly.
	 * Will Update nextIndex[] and matchIndex[] in the Routing table based off number of entries in hb.
	 * Will update the commitIndex if a majority of servers have committed at a given index
	 * by calling updateCommitIndex()
	 *
	 * @param hb
	 * @param rt
	 */
	public void receiveConfirmation(HeartBeat hb, RoutingTable rt) {

		Route route = hb.getResponderRoute();
		int num_entries = hb.getEntries().size();
		int start = rt.getNextIndex(route);

		for(int i = 0; i < num_entries; i++) { // Increment the number of nodes acknowledged at a given index
			int new_value = appendMatch.get(start+i) + 1;
			appendMatch.set(start + i, new_value);
			if(new_value >= rt.getMajority()) {
				updateCommitIndex(new_value);
			}
		}

		rt.updateServerIndex(route,num_entries); // update matchIndex[] and nextIndex[]
	}

	/**
	 * Given a majority of nodes have acknowledged a Log at a given index
	 * update the commitIndex accordingly
	 *
	 * @param new_index
	 */
	private void updateCommitIndex(int new_index) {
		for(int i = commitIndex; i <= new_index; i++) {
			commitToLogs(i);
		}
		this.commitIndex = Math.max(this.commitIndex, new_index);
	}

	/**
	 * A method to return all new logs entries thart have been queued in updateQueue List.
	 * Clears all entries from the updateQueue List.
	 *
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
	 *
	 * @param index
	 * @param term
	 * @return
	 */
	public Boolean confirmMatch(int index, Log term) {
		if(index < 0) {
			return term == null;
		}
		return logs.get(index).equals(term);
	}

	/**
	 * Given the commit Index from a Leader, update the commit index to be the min
	 * of the last applied log to our ledger and the leader's commit index.
	 *
	 * @param leader_commit_index
	 */
	public void syncCommitIndex(int leader_commit_index) {
		if(leader_commit_index > this.commitIndex) {
			this.commitIndex = Math.min(leader_commit_index,this.lastApplied);
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
	 * Commit a Log at a given index in the logs to the keyStore
	 *
	 * @param index
	 */
	private void commitToLogs(int index) {
		Log log = logs.get(index);
		updateKeyStore(log.getKey(),log.getValue());
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

    public Log getLogbyIndex(int index) {
		if(index < 0) {
			return null;
		}
		return logs.get(index);
	}


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

package ledger;

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

	private Map<String,String> keyStore; // Map all keys to values
	private List<Log> logs; // Store A Growing List of Log Values
	private List<Log> updateQueue; // a Queue storing all new Logs entries between Heartbeat broadcasts
	private HashMap<Log,Integer> commitMap;
	private final int MAJORITYPLACEHOLDER = 7;
	
	/**
	 * The Constructor for the Ledger Class
	 * 
	 */
	public Ledger() {
		keyStore = new HashMap<String,String>();
		logs = new ArrayList<Log>();
		updateQueue = new ArrayList<Log>();
	}


	
	/**
	 * A method to append new Log entries to the ledger. 
	 * <p>
	 * This allows a Leader to commit a log entry.
	 * 
	 * @param addition The new log to append to the Ledger.
	 */
	public void commitToLogs(Log addition) {
		logs.add(addition);
		// TODO: Need to search for followers' commits before updating key value store
		updateKeyStore(addition.getKey(),addition.getValue());
		updateQueue.add(addition);
	}

	/**
	 * This method is called to add a new log to the update queue.
	 * During regular hearbeat intervals the queue is emptied
	 * and all logs in the queu are sent over the network to
	 * follower nodes. Confirmation messages are expected from
	 * a majority of follower nodes before the log will be
	 * "commited" to the leader's data store.
	 *
	 */
	public void addToQueue(Log value) {
		updateQueue.add(value);
	}

	/**
	 * This method allows us to commit a log after receiving a confirmation.
	 * The corresponding matching value to a log will be decremented in a hashmap.
	 * if the the value becomes 0, then we delete the key-value pair from the
	 * confirmation HashMap and commit the log to our internal data store in this
	 * leader node.
	 *
	 */
	public void receiveConfirmation(Log log) {
		if(commitMap.containsKey(log)) {
			Integer value = commitMap.get(log);
			if(value == 0) {
				commitMap.remove(log);
				commitToLogs(log);
			} else {
				commitMap.put(log,value-1);
			}
		}
	}
	
	/**
	 * A method to return all new logs entries that have been queued in updateQueue List.
	 * Clears all entries from the updateQueue List. 
	 * 
	 * @return All logs that are stored in the member updateQueue Object.
	 */
	public List<Log> getUpdates() {
		List<Log> updates = new ArrayList<Log>();
		for(Log log : updateQueue) {
			commitMap.put(log,MAJORITYPLACEHOLDER); // TODO: Store the # which represents majority of nodes in file
			updates.add(log);
		}
		updateQueue = new ArrayList<Log>();
		return updates;
	}
	
	/**
	 * Update the HashMap Data structure representing the Key-Value store
	 * 
	 * @param key The lookup key of the data to be entered.
	 * @param value The value mapped to the lookup key of the data being entered.
	 */
	private void updateKeyStore(String key, String value) {
		keyStore.put(key, value);
	}
	
	/**
	 * This members prints all current logs that have been stored in the ledger.
	 * 
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
	
	/**
	 * Tests for the Ledger class. 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Ledger ledger = new Ledger();
		ledger.commitToLogs(new Log(1,2,"password","goats"));
		ledger.commitToLogs(new Log(2,3,"fire","hot"));
		ledger.printLogs();
	}
	
}

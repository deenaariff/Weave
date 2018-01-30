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
	private HashMap<Log,Integer> commit_map;
	
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
	 * Enables new log entries to be queued in the updateQueue Object. This allows a Leader
	 * class to store all log entries that need to be broadcasted to the cluster's followers between 
	 * Heartbeat intervals. A Follower object may not wish to store all newly added Heartbeat messages
	 * as they will not need to be broadcasted to the cluster. 
	 * 
	 * @param addition The new log to append to the Ledger.
	 * @param queue Determines whether to add a log to the queue of new entries.
	 */
	public void appendToLogs(Log addition, boolean queue) {
		logs.add(addition);
		if(queue == true) {
			// TODO: Need to search for followers' commits before updating key value store
			updateKeyStore(addition.getKey(),addition.getValue());
			updateQueue.add(addition);
		}
	}

	/**
	 * A method to add a log to a commit map.
	 * This will allow us to commit a log once it has been received.
	 *
	 */
	public boolean
	
	/**
	 * A method to return all new logs entries that have been queued in updateQueue List.
	 * Clears all entries from the updateQueue List. 
	 * 
	 * @return All logs that are stored in the member updateQueue Object.
	 */
	public List<Log> getUpdates() {
		List<Log> updates = new ArrayList<Log>();
		for(Log log : updateQueue) {
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
		ledger.appendToLogs(new Log(1,2,"password","goats"),false);	
		ledger.appendToLogs(new Log(2,3,"fire","hot"),false);	
		ledger.printLogs();
	}
	
}

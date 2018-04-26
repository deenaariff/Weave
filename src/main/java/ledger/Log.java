package ledger;

import java.io.Serializable;

/**
 * Log is a class that implements Serializable.
 * 
 * It represents an entry in a RaftNode's Ledger. New log's that a leader appends
 * to its ledger will be sent out in heartbeat messages to be appended
 * to the ledger's of rpc nodes.
 * 
 */
public class Log implements Serializable {
	
	// TODO: Check to see if we need to add hostname to the log
	private static final long serialVersionUID = 1L;
	private int term;
	private int index;
	private String key;
	private String value;
	
	/**
	 * This is the constructor for the Log class. 
	 * 
	 * @param term The term of the Log being instantiated.
	 * @param index The index of the Log being instantiated.
	 * @param key The key-lookup value of the Log being created.
	 * @param value The value to be paired with the key of the Log being creatd. 
	 */
	public Log (int term, int index, String key, String value) {
		this.term = term;
		this.index = index;
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Return the current term of the Log Object. 
	 * 
	 * @return The term member variable of the Log. 
	 */
	public int getTerm() {
		return this.term;
	}
		
	/**
	 * Returns index of the Log.
	 * 
	 * @return The index member variable of the log. 
	 */
	public int getIndex() {
		return this.index;
	}
	
	/**
	 * Update the key-value entry for a Log.
	 * 
	 * @param key The lookup key value of the key-value pair. 
	 * @param value The value to be paired to a lookup key. 
	 */
	public void updateKeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Returns the lookup key value of the current Log.
	 * 
	 * @return The value of key member variable. 
	 */
	public String getKey() {
		return this.key;
	}
	
	/**
	 * Returns the value of the key-value pair of the log. 
	 * 
	 * @return The value of the value member variable. 
	 */
	public String getValue() {
		return this.value;
	}

}

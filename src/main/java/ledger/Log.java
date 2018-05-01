package ledger;

import routing.Route;

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
	 * Update the key-value entry for a Log.
	 * @param key The lookup key value of the key-value pair. 
	 * @param value The value to be paired to a lookup key. 
	 */
	public void updateKeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}



	public int getTerm() {
		return this.term;
	}

	public int getIndex() {
		return this.index;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}



	@Override
	/**
	 * Override equals to determine equivalence based of key, value, and index
	 *
	 */
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!Log.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final Log to_be_matched = (Log) obj;

		boolean matchKey =  this.key == to_be_matched.getKey();
		boolean matchValue = this.value == to_be_matched.getValue();
		boolean matchIndex = this.index == to_be_matched.getIndex();

		return matchKey && matchValue && matchIndex;

	}

}

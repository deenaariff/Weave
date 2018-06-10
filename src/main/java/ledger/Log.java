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

	@Override
	/**
	 * Print the Value of the Log
	 */
	public String toString() {
		return this.key + "|" + this.value + "|" + this.index;
	}

	@Override
	/**
	 * Override equals to determine equivalence based of key, value, and index
	 */
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!Log.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final Log to_be_matched = (Log) obj;

		boolean matchKey = this.key.equals(to_be_matched.getKey());
		boolean matchValue = this.value.equals(to_be_matched.getValue());
		boolean matchIndex = this.index == to_be_matched.getIndex();

		return matchKey && matchValue && matchIndex;
	}


	/**
	 * Get the term stored in the log
	 * @return return the term of this log
	 */
	public int getTerm() {
		return this.term;
	}

	/**
	 * Get the index stored in the log
	 * @return return the index of this log
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Get the key stored in the log
	 * @return return the key of this log
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Get the value stored in the log
	 * @return return the value assigned to the key of this log
	 */
	public String getValue() {
		return this.value;
	}


}

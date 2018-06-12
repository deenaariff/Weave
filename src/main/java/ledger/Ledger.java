package ledger;

import messages.HeartBeat;
import messages.Vote;
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

    /**
     * The Constructor for the Ledger Class
     */
    public Ledger() {
        this.keyStore = new HashMap<>();
        this.logs = new ArrayList<>();
        this.appendMatch = new ArrayList<>();
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
     * Get the Logs in that are in the interval of a given start index, to the # num_logs after that
     *
     * @param start_index The start index of the first log to get
     * @param num_logs The number of logs to get from the given start index
     * @return a {@link List} of {@link Log} for the given start_index and num_logs
     */
    public List<Log> getLogs(int start_index, int num_logs) {
        List<Log> updates = new ArrayList<Log>();
        for(int i = 0; i < num_logs; i++) {
            if((start_index + i) < this.logs.size()) {
                updates.add(this.logs.get(start_index+i));
            }
        }
        return updates;
    }

    /**
     * This method is called by client to add new log to the list of logs.
     * This will be replicated to followers.
     *
     * @param value The {@link Log} to append to this nodes logs
     */
    public void addToLogs(Log value) {
        this.logs.add(value);
        this.appendMatch.add(0);
        this.lastApplied = this.logs.size();
    }

    /**
     * Receive a 'True' Heartbeat Message from a Follower and update Ledger accordingly.
     * Will Update nextIndex[] and matchIndex[] in the Routing table based off number of entries in hb.
     * Will update the commitIndex if a majority of servers have committed at a given index
     * by calling updateCommitIndex()
     *
     * @param hb the {@link HeartBeat} (response to AppendEntries RPC) to be confirmed by the leader
     * @param rt the {@link RoutingTable} to determine majority of cluster for commiting Log
     */
    public void receiveConfirmation(HeartBeat hb, RoutingTable rt) {

        Route route = hb.getResponderRoute();
        int num_entries = hb.getEntries().size();
        int start = hb.getPrevLogIndex();

        for(int i = 0; i < num_entries; i++) { // Increment the number of nodes acknowledged at a given index
            while(start + i >= appendMatch.size()) {
                appendMatch.add(0);
            }
            int new_value = this.appendMatch.get(start+i) + 1;
            this.appendMatch.set(start + i, new_value);
            if(new_value >= rt.getMajority()) {
                updateCommitIndex(start + i);
            }
        }

        rt.updateServerIndex(route,num_entries); // update matchIndex[] and nextIndex[]
    }

    /**
     * Given a majority of nodes have acknowledged a Log at a given index
     * update the commitIndex accordingly
     *
     * @param new_index The last index to be committed from the logs applied
     */
    private void updateCommitIndex(int new_index) {
        for(int i = this.commitIndex; i <= new_index; i++) {
            commitToLogs(i);
        }
        this.commitIndex = Math.max(this.commitIndex, new_index + 1);
    }


    /**
     * Confirms whether a Log at a given index matches the equivalent Log in the ledger
     *
     * @param index the index at which to confirm a match for a given {@link Log} entry
     * @param term the {@link Log} entry to match at the provided index
     * @return returns a Boolean based upon whether the term matches the logs at the given index
     */
    public Boolean confirmMatch(int index, Log term) {
        if(index == 0) {
            return term == null;
        } else if (index - 1 >= logs.size()) {
            return false;
        }
        return logs.get(index-1).equals(term);
    }

    /**
     * Given the commit Index from a Leader, update the commit index to be the min
     * of the last applied log to our ledger and the leader's commit index.
     *
     * @param leader_commit_index the leader_commit_index to update the follower's commit index
     */
    public void syncCommitIndex(int leader_commit_index) {
        if(leader_commit_index > this.commitIndex) {
            this.commitIndex = Math.min(leader_commit_index,this.lastApplied);
            for (int i = 0; i < commitIndex; i++) {
                Log entry = this.logs.get(i);
                updateKeyStore(entry.getKey(),entry.getValue());
            }
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
        int begin_entries = hb.getPrevLogIndex();
        for (int i = 0; i < hb.getEntries().size(); i++) {
            int update_index = begin_entries + i;
            if(update_index < logs.size()) {
                logs.set(update_index,hb.getEntries().get(i));
            } else {
                logs.add(hb.getEntries().get(i));
            }
        }
        this.lastApplied = begin_entries + hb.getEntries().size();
    }

    /**
     * Commit a Log at a given index in the logs to the keyStore
     *
     * @param index the index at which to commit a log
     */
    private void commitToLogs(int index) {
        Log log = logs.get(index);
        updateKeyStore(log.getKey(),log.getValue());
    }

    /**
     * Get the Log at a Given Index
     *
     * @param index The index to obtain a certain log in the node's logs
     * @return returns a {@link Log} that is at the given index in the node's logs
     */
    public Log getLogbyIndex(int index) {
        if(index < 0) {
            return null;
        }
        return logs.get(index);
    }

    /**
     * Get the last log in the ledger
     *
     * @return returns a {@link Log} at the end of the node's Logs
     */
    public Log getLastLog() {
        if(logs.size() > 0) {
            return logs.get(logs.size()-1);
        } else {
            return null;
        }
    }

    /**
     * Method for follower to validate Candidate's vote
     * If Candidate's log is at least as up to date as receiver's log grant vote
     *
     * @param vote The {@link Vote} to validate given the node's terms and last Log Term
     * @return returns a boolean one whether this vote is validated by the ledger's info
     */
    public boolean validateVote(Vote vote) {

        if(logs.size() == 0) {
            return true;
        }

        Log my_last_log = getLastLog();
        if (my_last_log.getTerm() < vote.getLastLogTerm()) {
            return true;
        } else if (my_last_log.getTerm() == vote.getLastLogTerm()) {
            return my_last_log.getIndex() <= vote.getLastLogIndex();
        }
        return false;
    }

    /**
     *
     * @return returns a Map that represents this node's key store
     */
    public Map<String,String> getKeyStore() {
        return this.keyStore;
    }

    /**
     *
     * @return returns the current commit index of this node
     */
    public int getCommitIndex() {
        return commitIndex;
    }

    /**
     *
     * @return returns the last index applied in the node's logs
     */
    public int getLastApplied() {
        return lastApplied;
    }



}

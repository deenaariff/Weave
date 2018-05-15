package voting_booth;

import Logger.Logger;
import info.HostInfo;
import routing.RoutingTable;

public class VotingBooth {

    private int votes_obtained = 0;
    private long start_election;
    private int election_interval;
    private HostInfo host_info;
    private Logger logger;

    private RoutingTable rt;

    public VotingBooth(RoutingTable rt, HostInfo host_info, Logger logger) {
        this.rt = rt;
        this.host_info = host_info;
        this.logger = logger;
    }

    public void reset() {
        this.votes_obtained = 0;
    }

    public int getVotes() {
        return this.votes_obtained;
    }

    public void incrementVotes() {
        this.votes_obtained += 1;
    }

    /**
     * This starts an election by incrementing the term, recording the start
     * time of the election, and then initializing the number of votes it has
     * obtained starting with its own vote for itself.
     *
     * Then in the main RaftNode thread, a RequestVote RPC will be broadcasted
     * to all other nodes the system.
     */
    public void startElection() {
        this.host_info.incrementTerm();
        this.election_interval = 200 * 1000;
        this.start_election = System.nanoTime();
        this.votes_obtained = 1;
    }

    /**
     * This method checks to see whether the duration of the election has
     * surpassed the random time interval for the election.
     *
     * @return boolean value describing whether election has timed out
     */
    public boolean isElectionOver() {
        return (System.nanoTime() - this.start_election) > this.election_interval;
    }

    /**
     * This method will check to see whether this node has received a majority
     * of votes.
     *
     * @return if majority received, return true
     *          if majority not received, return false
     */
    public boolean checkIfWonElection() {
        int totalTableLength = this.rt.getTable().size();
        this.host_info.setVotesObtained(this.votes_obtained);
        this.logger.log("Votes received: " + this.votes_obtained);
        return (this.votes_obtained >= totalTableLength/2 + 1);
    }

}

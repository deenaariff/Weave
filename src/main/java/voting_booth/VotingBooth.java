package voting_booth;

import logger.Logger;
import info.HostInfo;
import routing.Route;
import routing.RoutingTable;

import java.util.ArrayList;
import java.util.List;

public class VotingBooth {

    private int votes_obtained = 0;
    private long start_election;
    private int election_interval;
    private HostInfo host_info;
    private Logger logger;

    private final int ELECTION_TIMEOUT_MIN = 2000;
    private final int ELECTION_TIMEOUT_MAX = 3000;

    private RoutingTable rt;
    private List<Integer> voters;

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

    public void incrementVotes(int voter_id) {
        this.votes_obtained += 1;
        this.voters.add(voter_id);
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
        this.election_interval = ELECTION_TIMEOUT_MIN + (int)(Math.random() * (ELECTION_TIMEOUT_MAX - ELECTION_TIMEOUT_MIN));
        this.start_election = System.nanoTime();
        this.voters = new ArrayList<>();
        this.voters.add(this.host_info.getId());
        this.votes_obtained = 1;
    }

    /**
     * This method checks to see whether the duration of the election has
     * surpassed the random time interval for the election.
     *
     * @return boolean value describing whether election has timed out
     */
    public boolean isElectionOver() {
        boolean result = (System.nanoTime() - this.start_election) > (this.election_interval*1000);
        if(result) {
            this.logger.log("Election interval elapsed: (" + this.election_interval + " ms)");
        }
        return result;
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
        boolean result = (this.votes_obtained >= totalTableLength/2 + 1);
        return result;
    }

    public void printWon() {
        logger.log("Election Won, becoming Leader");
        printVotesObtained();
    }

    public void printLost() {
        logger.log("Election Lost, restarting election");
        printVotesObtained();
    }

    public void printVotesObtained() {
        logger.log("[" + host_info.getState() + "]: Received " + this.votes_obtained + " votes");
        for (Integer voter : this.voters) {
            Route route = rt.getRouteById(voter);
            logger.log("[" + host_info.getState() + "]: Received Vote From: Node " + voter + " " + route.printInfo());
        }
    }


}

package VotingBooth;

import info.HostInfo;
import routing.RoutingTable;

public class VotingBooth {

    private int votes_obtained = 0;
    private long start_election;

    private RoutingTable rt;

    public VotingBooth(RoutingTable rt) {
        this.rt = rt;
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

    public void startElection() {
        this.start_election = System.nanoTime();
        this.votes_obtained = 1;
    }

    public boolean isElectionOver() {
        long interval = 30000;
        return (System.nanoTime() - this.start_election) > interval;
    }

    public int endElection(HostInfo hostInfo) {
        int totalTableLength = this.rt.getTable().size();
        return (this.votes_obtained >= totalTableLength/2)? 1 : 0;
    }

}

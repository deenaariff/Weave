package state_helpers;

import Logger.Logger;
import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import messages.HeartBeat;
import messages.Vote;
import routing.Route;
import rpc.rpc;
import voting_booth.VotingBooth;

import java.io.IOException;
import java.net.ConnectException;

public class Follower {

    /**
     * This method handles heartbeats received by the follower.
     *
     * If a proper heartbeat is received (from leader, and equal or lower term),
     * the follower confirms that the previous log items match then sends a
     * confirmation
     *
     * @param hb
     * @param ledger
     * @param host_info
     * @throws IOException
     */
    public static void HandleHeartBeat(HeartBeat hb, Ledger ledger, HostInfo host_info) throws IOException {
        if (!hb.hasReplied()) {  // Ensure this is a heartbeat from a leader (yet to be acknowledged)
            if (host_info.getTerm() <= hb.getTerm()) {  // Check if equal or behind leader term
                int prevIndex = hb.getPrevLogIndex();
                Log prevLogTerm = hb.getPrevLog();

                // Ensure prevLog Term matches at given index
                if(ledger.confirmMatch(prevIndex, prevLogTerm) == true) {
                    new Logger(host_info).log("PrevLogTerm in HeartBeat Matches");
                    ledger.update(hb);
                    ledger.syncCommitIndex(hb.getLeaderCommitIndex());
                    hb.setReply(true);
                } else {
                    new Logger(host_info).log("PrevLogTerm in Does not Match");
                    new Logger(host_info).log("Heartbeat PrevLogTerm Index: " + hb.getPrevLogIndex());
                    new Logger(host_info).log("Log Size: " + ledger.getLastApplied());
                    hb.setReply(false);
                }
            } else {  // False if term is ahead of leader term
                hb.setReply(false);
            }

            Route origin = hb.getOriginRoute();

            // Update the origin info for the heartbeat on response
            hb.setTerm(host_info.getTerm());

            if (hb.getReply()) {  // Ensure my commitIndex is synced
                ledger.syncCommitIndex(hb.getLeaderCommitIndex());
            }

            RetryReturnHeartbeat(hb, origin);
        }
    }

    private static void RetryReturnHeartbeat(HeartBeat hb, Route origin) throws IOException {
        for (int i = 0; i < 1; i++) {
            try {
                rpc.returnHeartbeat(hb, origin);  // Return heartbeat to the destination
                break;
            } catch (ConnectException e) {
                System.out.println("Retry Heartbeat Failed");
                // Do nothing
            }
        }
    }

    /**
     * This method handles incoming votes to the follower.
     *
     * If the vote is coming from a valid (higher term) candidate, it will cast
     * its vote (if it hasn't already) and return the vote.
     *
     * If it is not a valid candidate, return the vote without casting a vote.
     *
     * @param vote
     * @param vb
     * @param host_info
     */
    public static void HandleVote(Vote vote, VotingBooth vb, HostInfo host_info, Ledger ledger) throws IOException {
        Logger logger = new Logger(host_info);

        boolean valid_term = (vote.getTerm() >= host_info.getTerm());
        boolean up_to_date = ledger.validateVote(vote);

        if (!host_info.hasVoted() && valid_term && up_to_date) {  // Check if valid candidate
            vote.castVote(host_info.getId());
            host_info.setVoteFlag(true);
            host_info.setVote(vote.getRoute());
            logger.log("Voting For - " + vote.getHostName() + ":" + vote.getVotingPort());
        }

        RetryVote(vote);
    }

    /**
     *
     *
     * @param vote
     * @throws IOException
     */
    private static void RetryVote(Vote vote) throws IOException {
        for (int i = 0; i < 1; i++) {
            try {
                rpc.returnVote(vote);  // Send vote back
                break;
            } catch (ConnectException e) {
                System.out.println("Retry Vote Failed");
                // Do nothing
            }
        }
    }

}

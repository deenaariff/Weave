package state_helpers;

import logger.Logger;
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
     * @param hb The {@link HeartBeat} Object this method handles
     * @param ledger The {@link Ledger} Object of the node
     * @param host_info The {@link HostInfo} Object of the node
     * @throws IOException Throws IO Exception from RetryReturnHeartBeat()
     */
    public static void HandleHeartBeat(HeartBeat hb, Ledger ledger, HostInfo host_info) throws IOException {
        Logger logger = host_info.getLogger();

        if (!hb.hasReplied()) {  // Ensure this is a heartbeat from a leader (yet to be acknowledged)
            if (host_info.getTerm() <= hb.getTerm()) {  // Check if equal or behind leader term
                int prevIndex = hb.getPrevLogIndex();
                Log prevLogTerm = hb.getPrevLog();

                // Ensure prevLog Term matches at given index
                if(ledger.confirmMatch(prevIndex, prevLogTerm) == true) {
                    if (prevLogTerm != null) {
                        logger.log("PrevLogTerm in HeartBeat Matches - " + prevLogTerm.toString());
                    } else {
                        logger.log("PrevLogTerm in HeartBeat Matches - NULL");
                    }
                    ledger.update(hb);
                    ledger.syncCommitIndex(hb.getLeaderCommitIndex());
                    hb.setReply(true);
                } else {
                    logger.log("PrevLogTerm in Does not Match");
                    logger.log("Heartbeat PrevLogTerm Index: " + hb.getPrevLogIndex());
                    logger.log("Log Size: " + ledger.getLastApplied());
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

            RetryReturnHeartbeat(hb, origin, logger);
        }
    }

    private static void RetryReturnHeartbeat(HeartBeat hb, Route origin, Logger logger) throws IOException {
        for (int i = 0; i < 1; i++) {
            try {
                rpc.returnHeartbeat(hb, origin);  // Return heartbeat to the destination
                break;
            } catch (ConnectException e) {
                logger.log("Retry Heartbeat Failed");
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
     * @param vote The {@link Vote} Object this method handles
     * @param vb The {@link VotingBooth} of the Node
     * @param host_info The {@link HostInfo} of the Node
     * @param ledger The {@link Ledger} of the node
     * @throws IOException Throws IO Exception from RetryVote()
     */
    public static void HandleVote(Vote vote, VotingBooth vb, HostInfo host_info, Ledger ledger) throws IOException {
        Logger logger = host_info.getLogger();

        boolean valid_term = (vote.getTerm() >= host_info.getTerm());
        boolean up_to_date = ledger.validateVote(vote);

        if (!host_info.hasVoted() && valid_term && up_to_date) {  // Check if valid candidate
            vote.castVote(host_info.getId());
            host_info.setVoteFlag(true);
            host_info.setVote(vote.getRoute());
            logger.log("Voting For - " + vote.getHostName() + ":" + vote.getVotingPort());
        }

        RetryVote(vote, logger);
    }

    /**
     * Retries respond to Votes if the first fails
     *
     * @param vote The {@link Vote} Object this method will retry
     * @throws IOException
     */
    private static void RetryVote(Vote vote, Logger logger) throws IOException {
        for (int i = 0; i < 1; i++) {
            try {
                rpc.returnVote(vote);  // Send vote back
                break;
            } catch (ConnectException e) {
                logger.log("Retry Vote Failed");
                // Do nothing
            }
        }
    }

}

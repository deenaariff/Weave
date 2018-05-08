package rpc;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import messages.HeartBeat;
import messages.Vote;
import routing.Route;
import routing.RoutingTable;
import state_helpers.Leader;

public class rpc {

    /**
     *
     * @param rt
     * @param ledger
     * @param host_info
     */
    public static void broadcastHeartbeatUpdates(RoutingTable rt, Ledger ledger, HostInfo host_info) {
        for (Route route : rt.getTable()) {
            if (!host_info.matchRoute(route)) { // Ensure you don't send messages to yourself
                try {
                    List<Log> updates = Leader.determineUpdates(route, rt, ledger);
                    HeartBeat hb = new HeartBeat(host_info, updates, route, rt, ledger); // create a new heartbeat
                    sendHeartbeat(hb, route.getIP(), route.getHeartbeatPort());

                    System.out.println("[" + host_info.getState() + "]: Sending Updates to " + route.getIP() + ":" + route.getHeartbeatPort());
                } catch (ConnectException e) {
                    System.out.println("[" + host_info.getState() + "]: Error - Unable Connect to " + route.getIP() + ":" + route.getHeartbeatPort());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * @param rt
     * @param host_info
     */
    public static void broadcastVotes(RoutingTable rt, HostInfo host_info) {
        int totalTableLength = rt.getTable().size();

        // Send New Vote Objects to all nodes in the routing Table.
        System.out.println("[" + host_info.getState() + "]: Requesting Votes from " + totalTableLength +  " Hosts");

        for (Route route : rt.getTable()) {
            Vote newVote = new Vote(host_info);
            if (host_info.matchRoute(route) == false) {  // Add logic to not send to yourself
                try {
                    sendVote(newVote, route.getIP(), route.getVotingPort());
                } catch (ConnectException e) { // do Nothing as non_response will be Handled by listener
                    System.out.println("[" + host_info.getState() + "]: Failed to Connect To " + route.getIP() + " at Voting Port " + route.getVotingPort());
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    /**
     *
     * @param hb
     * @param hostName
     * @param portNumber
     * @throws IOException
     */
    public static void sendHeartbeat(HeartBeat hb, String hostName, Integer portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);
        final OutputStream outputStream = socket.getOutputStream();
        final ObjectOutputStream output = new ObjectOutputStream(outputStream);
        output.writeObject(hb);
        socket.close();
    }

    /**
     *
     * @param vote
     * @param hostName
     * @param portNumber
     * @throws IOException
     */
    public static void sendVote (Vote vote, String hostName, int portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);
        final OutputStream outputStream = socket.getOutputStream();
        final ObjectOutputStream output = new ObjectOutputStream(outputStream);
        output.writeObject(vote);
        socket.close();
    }

    /**
     *
     * @param hb
     * @param destination
     * @throws IOException
     */
	public static void returnHeartbeat(HeartBeat hb, Route destination) throws IOException {
        sendHeartbeat(hb,destination.getIP(),destination.getHeartbeatPort());
	}

    /**
     *
     * @param vote
     * @throws IOException
     */
    public static void returnVote(Vote vote) throws IOException {
        Route vote_route = vote.getRoute();
        sendVote(vote,vote_route.getIP(),vote_route.getVotingPort());
    }


}

package rpc;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;

import info.HostInfo;
import ledger.Ledger;
import ledger.Log;
import messages.HeartBeat;
import messages.Vote;
import routing.Route;
import routing.RoutingTable;

public class rpc {

    /**
     * This method broadcasts a heartbeat (which carries a list of updates)
     * to all nodes in the distributed system.
     *
     * @param rt routing table with all host info
     * @param ledger ledger which maintains key-value store
     * @param host_info info about this node
     */
    public static void broadcastHeartbeatUpdates(RoutingTable rt, Ledger ledger, HostInfo host_info) {
        List<Log> updates = ledger.getUpdates();
        for (Route route : rt.getTable()) {
            if (host_info.matchRoute(route) == false) {
                try {
                    HeartBeat hb = new HeartBeat(host_info, updates, route, rt, ledger);
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

    public static void broadcastVotes(RoutingTable rt, HostInfo host_info) {
        int totalTableLength = rt.getTable().size();
        int messagesReceived = 0;

        // Send New Vote Objects to all nodes in the routing Table.
        System.out.println("[" + host_info.getState() + "]: Requesting Votes from " + totalTableLength +  " Hosts");

        for (Route route : rt.getTable()) {
            Vote newVote = new Vote(host_info);
            // Add logic to not send to yourself
            if (host_info.matchRoute(route) == false) {
                try {
                    sendVote(newVote, route.getIP(), route.getVotingPort());
                } catch (ConnectException e) {
                    System.out.println("[" + host_info.getState() + "]: Failed to Connect To " + route.getIP() + " at Voting Port " + route.getVotingPort());
                    // do Nothing as non_response will be Handled by listener
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


    public static void sendHeartbeat(HeartBeat hb, String hostName, Integer portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);
        final OutputStream outputStream = socket.getOutputStream();
        final ObjectOutputStream output = new ObjectOutputStream(outputStream);
        output.writeObject(hb);
        socket.close();
    }

    public static void sendVote (Vote vote, String hostName, int portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);
        final OutputStream outputStream = socket.getOutputStream();
        final ObjectOutputStream output = new ObjectOutputStream(outputStream);
        output.writeObject(vote);
        socket.close();
    }

	public static void returnHeartbeat(HeartBeat hb, Route destination) throws IOException {
        sendHeartbeat(hb,destination.getIP(),destination.getHeartbeatPort());
	}

    public static void returnVote(Vote vote) throws IOException {
        Route vote_route = vote.getRoute();
        sendVote(vote,vote_route.getIP(),vote_route.getVotingPort());
    }


}

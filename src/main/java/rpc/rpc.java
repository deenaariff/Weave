package rpc;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
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
     * This method broadcasts a heartbeat (which carries a list of updates)
     * to all nodes in the distributed system.
     *
     * @param rt routing table with all host info
     * @param ledger ledger which maintains key-value store
     * @param host_info info about this node
     */
    public static void broadcastHeartbeatUpdates(RoutingTable rt, Ledger ledger, HostInfo host_info) {
        for (Route route : rt.getTable()) {
            if (!host_info.matchRoute(route)) { // Ensure you don't send messages to yourself
                try {
                    List<Log> updates = Leader.determineUpdates(route, rt, ledger);
                    HeartBeat hb = new HeartBeat(host_info, updates, route, rt, ledger); // create a new heartbeat
                    sendHeartbeat(hb, route.getIP(), route.getHeartbeatPort());
                    System.out.println(hb.toString());
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
    public static void broadcastVotes(RoutingTable rt, HostInfo host_info, Ledger ledger) {
        int totalTableLength = rt.getTable().size();

        // Send New Vote Objects to all nodes in the routing Table.
        System.out.println("[" + host_info.getState() + "]: Requesting Votes from " + totalTableLength +  " Hosts");

        for (Route route : rt.getTable()) {
            if (host_info.matchRoute(route) == false) {  // Add logic to not send to yourself
                Vote newVote = new Vote(host_info, ledger);
                System.out.println("--------" + route.getIP() + ":" +  route.getEndpointPort() + "--------------");
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

    /**
     * Notify all watchers that a leader election has occured
     *
     * @param host_info
     * @param rt
     */
    public static void notifyElectionChange(HostInfo host_info, RoutingTable rt) {
        System.out.println("We have " + rt.getWatchers().size() + " watchers");
        if(rt.hasWatchers()) {
            try {
                for (Route watcher : rt.getWatchers()) {
                    Socket socket = new Socket(watcher.getIP(), watcher.getEndpointPort());

                    HashMap<String, Object> notif = new HashMap();
                    notif.put("cmd","leader");
                    notif.put("leader_IP", host_info.getRoute().getIP());
                    notif.put("leader_port",host_info.getRoute().getEndpointPort());
                    notif.put("election_time",System.nanoTime());

                    String response = JsonUtil.toJson(notif) + "\n";
                    System.out.println("Notifying Watcher : " + response);

                    final OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    osw.write(response, 0, response.length());
                    osw.flush();
                    osw.close();
                }
            } catch (ConnectException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    /**
     * Send a response to a Client when is Socket Client Controller Mode
     *
     * @param socket
     * @param response
     * @throws IOException
     */
    public static void sendClientResponse(Socket socket, String response) throws IOException {
        final OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        osw.write(response, 0, response.length());
        osw.flush();
        osw.close();
    }
}

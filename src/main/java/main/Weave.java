package main;
import client_controller.AsyncSocketClientController;
import client_controller.RestClientController;
import client_controller.SyncSocketClientController;
import logger.Logger;
import info.HostInfo;
import ledger.Ledger;
import node.RaftNode;

import routing.Route;
import routing.RoutingTable;
import rpc_heartbeat.HeartbeatListener;
import rpc_vote.VotingListener;
import voting_booth.VotingBooth;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The Main Class to Run a Weave node
 * 
 * @author deenaariff
 */
public class Weave {

    public static void main(String[] args) {

        Ledger ledger = new Ledger();

        String file = "/Users/deenaariff/Documents/DVKS/Raft/src/main/resources/nodes.xml";
        if (args.length >= 3) {
            file = args[2];
        }

        RoutingTable rt = new RoutingTable(file); //Load the Routing Table Info from nodes.xml
        Route route = rt.getRouteById(Integer.parseInt(args[0])); // Get this Nodes Routing Info

        HostInfo host = new HostInfo(route);
        Logger logger = host.getLogger();
        VotingBooth vb = new VotingBooth(rt, host, logger);

        // Initializer Method
        logger.log("IP Address: " + route.getIP());
        logger.log("Listening on PORT: " + route.getEndpointPort());
        logger.log("HeartBeat PORT: " + route.getHeartbeatPort());
        logger.log("Voting PORT: " + route.getVotingPort());
        logger.log("Starting Weave - A Learning Environment for the RAFT Consensus Algorithm");

        int socket_controller = 0;

        if (args.length >= 2) {
            socket_controller = Integer.parseInt(args[1]);
        }

        // Start the Spark Java Listener
        if(socket_controller == 0) {
            RestClientController server = new RestClientController(host, ledger, rt);
            server.listen();
        }

        // Create all Threads
        Thread hb_thread = new Thread(new HeartbeatListener(host, ledger, rt, vb));
        Thread voting_thread = new Thread(new VotingListener(host, rt, vb, ledger));
        Thread main_thread = new Thread(new RaftNode(rt, ledger, host, vb));

        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.execute(hb_thread);
        executor.execute(voting_thread);
        executor.execute(main_thread);

        if(socket_controller == 1) {
            SyncSocketClientController server = new SyncSocketClientController(host, ledger);
            server.listen();
        } else if (socket_controller == 2) {
            AsyncSocketClientController server = new AsyncSocketClientController(host,ledger);
            server.listen();
        }

    }

}

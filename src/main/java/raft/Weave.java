package raft;
import Logger.Logger;
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

	    // Weave Dependencies
		Ledger ledger = new Ledger();

        RoutingTable rt = new RoutingTable("nodes.xml", ledger); //Load the Routing Table Info from nodes.xml
        Route route = rt.getRouteById(Integer.parseInt(args[0])); // Get this Nodes Routing Info

        HostInfo host = new HostInfo(route);
        Logger logger = new Logger(host);
        VotingBooth vb = new VotingBooth(rt,host, logger);


        // Initializer Method
        logger.log("IP Address: " + route.getIP());
        logger.log("Listening on PORT: " + route.getEndpointPort());
        logger.log("HeartBeat PORT: " + route.getHeartbeatPort());
        logger.log("Voting PORT: " + route.getVotingPort());
		logger.log("Starting Weave Consensus Algorithm");


		// Create all Threads
        Thread hb_thread = new Thread(new HeartbeatListener(host,ledger,rt,vb, logger));
        Thread voting_thread = new Thread(new VotingListener(host,rt,vb, logger));
        Thread main_thread = new Thread(new RaftNode(rt,ledger,host));

        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.execute(hb_thread);
        executor.execute(voting_thread);
        executor.execute(main_thread);

        // Start the Spark Java Listener
        ClientController server = new ClientController(host, ledger, route, rt);
        server.listen();

	}

}

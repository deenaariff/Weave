package raft;
import info.HostInfo;
import ledger.Ledger;
import node.RaftNode;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import routing.Route;
import routing.RoutingTable;
import rpc_heartbeat.HeartbeatListener;
import rpc_vote.VotingListener;
import voting_booth.VotingBooth;


/**
 * The Main Class to Run a Weave node
 * 
 * @author deenaariff
 */
@SpringBootApplication
public class Weave {

	public static void main(String[] args) {

	    // Weave Dependencies
		Ledger ledger = new Ledger();

        RoutingTable rt = new RoutingTable("nodes.xml", ledger); //Load the Routing Table Info from nodes.xml
        Route route = rt.getRouteById(Integer.parseInt(args[0])); // Get this Nodes Routing Info

        HostInfo host = new HostInfo(route, rt);
        VotingBooth vb = new VotingBooth(rt,host);
        host.setVotingBooth(vb);

        // Initializer Method
        System.out.println("IP Address: " + route.getIP());
        System.out.println("Listening on PORT: " + route.getEndpointPort());
        System.out.println("HeartBeat PORT: " + route.getHeartbeatPort());
        System.out.println("Voting PORT: " + route.getVotingPort());
		System.out.println("Starting Weave Consensus Algorithm");


		// Create all Threads
        Thread hb_thread = new Thread(new HeartbeatListener(host,ledger,rt));
        Thread voting_thread = new Thread(new VotingListener(host,rt,vb));
        Thread main_thread = new Thread(new RaftNode(rt,ledger,host));

        hb_thread.start();
        voting_thread.start();
        main_thread.start();


        // Start the Spark Java Listener
        ClientController server = new ClientController(host, ledger, route);
        server.listen();

	}

}

package raft;
import configuration.Configuration;
import ledger.Ledger;
import node.RaftNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import routing.Route;
import routing.RoutingTable;

/**
 * The Main Class to Run a Raft node
 * 
 * @author deenaariff
 */
@SpringBootApplication
public class Raft {


	public static void main(String[] args) {

		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		Ledger ledger = (Ledger) context.getBean("ledger");

		/* Load the Routing Table Info from nodes.xml */
        RoutingTable rt = new RoutingTable("nodes.xml");

        /* Get this Nodes Routing Info */
        Route route = rt.getRouteById(1);

        System.out.println("IP Address: " + route.getIP());
        System.out.println("Listening on PORT: " + route.getEndpointPort());
        System.out.println("HeartBeat PORT: " + route.getVotingPort());
        System.out.println("Voting PORT: " + route.getHeartbeatPort());

		RaftNode node = new RaftNode(rt, ledger,route.getHeartbeatPort(),route.getVotingPort());
		
		System.out.println("Starting Raft Consensus Algorithm");

		// Start the Client Service API
		SpringApplication.run(Raft.class, args);
		
		// Run the State Machine
		while (true) {
			System.out.println(ledger.getUpdates());
			if (node.getHostInfo().isLeader()) node.runLeader();				
			else if (node.getHostInfo().isCandidate()) node.runCandidate();				
			else if (node.getHostInfo().isFollower()) node.runFollower();								
		}
		
	}

}

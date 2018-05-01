package raft;
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

        RoutingTable rt = new RoutingTable("nodes.xml", ledger); //Load the Routing Table Info from nodes.xml

        Route route = rt.getRouteById(Integer.parseInt(args[0])); // Get this Nodes Routing Info

        System.out.println("IP Address: " + route.getIP());
        System.out.println("Listening on PORT: " + route.getEndpointPort());
        System.out.println("HeartBeat PORT: " + route.getHeartbeatPort());
        System.out.println("Voting PORT: " + route.getVotingPort());

		RaftNode node = new RaftNode(rt, ledger, route);
		
		System.out.println("Starting Raft Consensus Algorithm");

		/*SpringApplication.run(Raft.class, args);*/ 		// Start the Client Service API

		node.run(); // Run the State Machine
		
	}

}

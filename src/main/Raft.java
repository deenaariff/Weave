package main;
import node.RaftNode;

/**
 * The Main Class to Run a Raft node
 * 
 * @author deenaariff
 */
public class Raft {

	public static void main(String[] args) {
		
		// Create a new Raft Node
		RaftNode node = new RaftNode();
		
		System.out.println("Starting Raft Consensus Algorithm");
		
		// Run the State Machine 
		while (true) {	
			if (node.getHostInfo().isLeader()) node.runLeader();				
			else if (node.getHostInfo().isCandidate()) node.runCandidate();				
			else if (node.getHostInfo().isFollower()) node.runFollower();								
		}
		
	}

}
